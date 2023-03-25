package com.cristal.ble;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cristal.ble.ui.LoginFragment;
import com.cristal.ble.ui.RegisterFragment;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.cristal.ble.adapter.DeviceAdapter;
import com.cristal.ble.comm.ObserverManager;
import com.cristal.ble.operation.OperationActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        RegisterFragment.FragmentInteractionListener,
        LoginFragment.FragmentInteractionListener{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int BLE_SCAN_STATUS = 1;
    private static final int BLE_STOP_SCAN_STATUS = 0;
    private int bleScanStatus = BLE_STOP_SCAN_STATUS;

    private DrawerLayout mDrawerLayout;
    private NavigationView navView;

    private LinearLayout settingLayout;
    private EditText setNameEdit, setMacEdit, setUuidEdit;
    private Switch setAutoConnectSw;

    private MenuItem scanMenuItem;

    public DeviceAdapter mDeviceAdapter;

    private ProgressDialog progressDialog;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;      // RecyclerView

    private List<BleDevice> bleDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getSupportFragmentManager().beginTransaction().add(R.id.fragment_con,PlayerUi.newInstance(),"music player").commit();
        checkPermissions();

        initView();

        // BLE Initialization and configuration
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        signup();
    }

    public void signup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment(), "RegisterFragment")
                .addToBackStack("RegisterFragment")
                .commit();
    }

    @Override
    public void onRegisterSuccess() {
    }

    public void signin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment(), "LoginFragment")
                .addToBackStack("LoginFragment")
                .commit();
    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_toolbar, menu);
        scanMenuItem = menu.findItem(R.id.action_scan_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:  // Side-slip menu

                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_scan_item:
                if (bleScanStatus == BLE_STOP_SCAN_STATUS) { // Start scanning

                    bleSetScanRule();
                    bleStartScan();
                } else if (bleScanStatus == BLE_SCAN_STATUS) { // Stop scanning

                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.action_show_log_item:
                Toast.makeText(this, "show log", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * View initialization
     */
    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.device_drawer_layout);

        // BLE scanning and connection Layout
//        settingLayout = (LinearLayout) findViewById(R.id.ble_setting_layout);
//        settingLayout.setVisibility(View.GONE);
        setNameEdit = (EditText) findViewById(R.id.set_ble_scan_name_et);
        setMacEdit = (EditText) findViewById(R.id.set_ble_scan_mac_et);
        setUuidEdit = (EditText) findViewById(R.id.set_ble_scan_uuid_et);
        setAutoConnectSw = (Switch) findViewById(R.id.set_ble_auto_reconnect_sw);

        // Pull down to refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Pull down to refresh to achieve BLE scanning function
                bleSetScanRule();
                bleStartScan();
            }
        });

        // Side-slip menu
        navView = (NavigationView) findViewById(R.id.nav_view);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  // Show navigation buttons
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);  // Set navigation button icon
        }
        navView.setCheckedItem(R.id.nav_setting);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // 侧滑菜单的菜单项选择事件处理
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                switch (menuItem.getItemId()) {
                    case R.id.nav_setting:
                        Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
//                        settingLayout.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                        break;
                    case R.id.nav_device_info:
                        Toast.makeText(MainActivity.this, "Device Information", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });


        progressDialog = new ProgressDialog(this);



        recyclerView = (RecyclerView) findViewById(R.id.device_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mDeviceAdapter = new DeviceAdapter(bleDeviceList);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    bleConnect(bleDevice);
                }

                Log.d(TAG, "onConnect: " + bleDevice.getName());
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                // Parse the broadcast packet

                Log.d(TAG, "onDetail: " + bleDevice.getName());
            }
        });
        recyclerView.setAdapter(mDeviceAdapter);
    }

    /**
     *
     * Show connectable BLE devices
     */
    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    /**
     * Check whether the permissions required to run the App have been obtained
     */
    private void checkPermissions() {

        //
        //Check if BLE is turned on
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "You should open Bluetooth", Toast.LENGTH_SHORT).show();
        }

        //
        //Required permissions
        String[] perms = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        //
        //Check if the permission has been obtained
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "App has obtained the required permissions", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "You should agree all the needed permissions if you want to use this App.",
                    0, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Success obtain permissions: " + perms, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Fail obtain permissions:" + perms, Toast.LENGTH_SHORT).show();

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    /**
     *
     * Set BLE scanning rules
     */
    private void bleSetScanRule() {
        // UUID
        String[] uuids;
        String str_uuid = setUuidEdit.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        // Name
        String[] names;
        String str_name = setNameEdit.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        // MAC
        String mac = setMacEdit.getText().toString();

        // AutoConnect
        boolean isAutoConnect = setAutoConnectSw.isChecked();

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // Only scan the devices of the specified service, optional
                .setDeviceName(true, names)    // Only scan devices with specified broadcast name, optional
                .setDeviceMac(mac)                  // Only scan devices of specified mac, optional
                .setAutoConnect(isAutoConnect)      // AutoConnect parameter when connecting, optional, default false
                .setScanTimeOut(10000)              // Scan timeout time, optional, default 10 seconds
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    /**
     * Start BLE scan
     */
    private void bleStartScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

                bleDeviceList.clear();

                mDeviceAdapter.clear();
                mDeviceAdapter.notifyDataSetChanged();

                recyclerView.removeAllViews();

//                settingLayout.setVisibility(View.GONE);

                swipeRefreshLayout.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);

                scanMenuItem.setTitle(getString(R.string.stop_scan));

                bleScanStatus = BLE_SCAN_STATUS;
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if(bleDevice.getName() !=null && bleDevice.getName().contains("CRISTAL")){

                    System.out.println("this is from manin --> "+bleDevice.getName());

                    mDeviceAdapter.addDevice(bleDevice);
                    mDeviceAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                scanMenuItem.setTitle(getString(R.string.start_scan));

                swipeRefreshLayout.setRefreshing(false);

                bleScanStatus = BLE_STOP_SCAN_STATUS;
            }
        });
    }

    /**
     * Connect BLE device
     * @param bleDevice
     */
    public void bleConnect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.setMessage("Connecting");  // Set the BLE connection ProgressDialog prompt text
                progressDialog.show();  // Show BLE connection ProgressDialog
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                scanMenuItem.setTitle(getString(R.string.start_scan));

                progressDialog.dismiss();  // Hide BLE connection ProgressDialog

                bleScanStatus = BLE_SCAN_STATUS;

                Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();  // Hide BLE connection ProgressDialog

                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                Intent intent = new Intent(MainActivity.this, OperationActivity.class);
                intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                startActivity(intent);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();  // Hide BLE connection ProgressDialog

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, "You disconnect BLE", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "BLE was disconnected", Toast.LENGTH_SHORT).show();

                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    /**
     * Check if the phone GPS is turned on
     * @return
     */
    private boolean checkGpsIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGpsIsOpen()) {
                bleSetScanRule();
                bleStartScan();
            }
        }
    }
}
