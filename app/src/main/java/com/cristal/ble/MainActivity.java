package com.cristal.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.cristal.ble.api.ApiRepository;
import com.cristal.ble.api.LoginResponse;
import com.cristal.ble.api.cristalcloudImgResponce;
import com.cristal.ble.ui.LoginFragment;
import com.cristal.ble.ui.RegisterFragment;
import com.cristal.ble.ui.ScanFragment;
import com.cristal.ble.ui.player.OperationActivity;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        RegisterFragment.FragmentInteractionListener,
        LoginFragment.FragmentInteractionListener{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private static final int BLE_SCAN_STATUS = 1;
    private static final int BLE_STOP_SCAN_STATUS = 0;
    private int bleScanStatus = BLE_STOP_SCAN_STATUS;

//    private DrawerLayout mDrawerLayout;
//    private NavigationView navView;

    private LinearLayout settingLayout;
    private EditText setNameEdit, setMacEdit, setUuidEdit;
    private Switch setAutoConnectSw;

    private MenuItem scanMenuItem;

//    public DeviceAdapter mDeviceAdapter;

//    private ProgressDialog progressDialog;

//    private SwipeRefreshLayout swipeRefreshLayout;
//    private RecyclerView recyclerView;      // RecyclerView

    private List<BleDevice> bleDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AppPreference(this);

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
    }

    public void signup() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment(), "RegisterFragment")
                .commit();
    }

    public void signin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment(), "LoginFragment")
                .commit();
    }
    private void getToken( String email, String password) {


        ApiRepository.login(email, password, new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                if (response.body() != null) {
                    LoginResponse body = response.body();
                    System.out.println("--java-> login" + body);
                    System.out.println("---> " + body);
                    if (body.getSuccess()) {
                        AppPreference.preference.setLoginResponse(body);
                        System.out.println("---> login " + body.getToken());
//                        imageByteArray = body.getData().getImg();

                    } else {
                        System.out.println("---> login failed");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LoginResponse> call, Throwable t) {
                System.out.println("---> login ERROR" + t);
            }
        });

    }
    @Override
    public void onLoginSuccess() {
        scan();
    }

    public void scan() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ScanFragment(), "ScanFragment")
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        showConnectedDevice();

        if (AppPreference.preference != null && AppPreference.preference.getLoginResponse() != null) {
            scan();
            getToken(AppPreference.preference.getLoginRequest().getEmail(), AppPreference.preference.getLoginRequest().getPassword());

        } else {
            signin();
        }
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
//        switch (item.getItemId()) {
//            case android.R.id.home:  // Side-slip menu
//
//                mDrawerLayout.openDrawer(GravityCompat.START);
//                break;
//            case R.id.action_scan_item:
//                if (bleScanStatus == BLE_STOP_SCAN_STATUS) { // Start scanning
//
//                    bleSetScanRule();
//                    bleStartScan();
//                } else if (bleScanStatus == BLE_SCAN_STATUS) { // Stop scanning
//
//                    BleManager.getInstance().cancelScan();
//                }
//                break;
//            case R.id.action_show_log_item:
//                Toast.makeText(this, "show log", Toast.LENGTH_SHORT).show();
//                break;
//            default:
//                break;
//        }
        return true;
    }


    /**
     * View initialization
     */
    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.device_toolbar);
        setSupportActionBar(toolbar);

//        mDrawerLayout = (DrawerLayout) findViewById(R.id.device_drawer_layout);

        // BLE scanning and connection Layout
//        settingLayout = (LinearLayout) findViewById(R.id.ble_setting_layout);
//        settingLayout.setVisibility(View.GONE);
        setNameEdit = (EditText) findViewById(R.id.set_ble_scan_name_et);
        setMacEdit = (EditText) findViewById(R.id.set_ble_scan_mac_et);
        setUuidEdit = (EditText) findViewById(R.id.set_ble_scan_uuid_et);
        setAutoConnectSw = (Switch) findViewById(R.id.set_ble_auto_reconnect_sw);

//        // Pull down to refresh
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.device_swipe_refresh);
//        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
//        swipeRefreshLayout.setVisibility(View.VISIBLE);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                //Pull down to refresh to achieve BLE scanning function
//                bleSetScanRule();
//                bleStartScan();
//            }
//        });

        // Side-slip menu
//        navView = (NavigationView) findViewById(R.id.nav_view);
//        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);  // Show navigation buttons
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);  // Set navigation button icon
//        }
//        navView.setCheckedItem(R.id.nav_setting);
//        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//
//                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
//
//                switch (menuItem.getItemId()) {
//                    case R.id.nav_setting:
//                        Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
////                        settingLayout.setVisibility(View.VISIBLE);
//                        swipeRefreshLayout.setVisibility(View.GONE);
//                        break;
//                    case R.id.nav_device_info:
//                        Toast.makeText(MainActivity.this, "Device Information", Toast.LENGTH_SHORT).show();
//                        break;
//
//                    default:
//                        break;
//                }
//
//                mDrawerLayout.closeDrawers();
//                return true;
//            }
//        });


//        progressDialog = new ProgressDialog(this);



//        recyclerView = (RecyclerView) findViewById(R.id.device_recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        mDeviceAdapter = new DeviceAdapter(bleDeviceList);
//        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
//            @Override
//            public void onConnect(BleDevice bleDevice) {
//                if (!BleManager.getInstance().isConnected(bleDevice)) {
//                    BleManager.getInstance().cancelScan();
//                    bleConnect(bleDevice);
//                }
//
//                Log.d(TAG, "onConnect: " + bleDevice.getName());
//            }
//
//            @Override
//            public void onDetail(BleDevice bleDevice) {
//                // Parse the broadcast packet
//
//                Log.d(TAG, "onDetail: " + bleDevice.getName());
//            }
//        });
//        recyclerView.setAdapter(mDeviceAdapter);
    }

    /**
     *
     * Show connectable BLE devices
     */
//    private void showConnectedDevice() {
//        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
//        mDeviceAdapter.clearConnectedDevice();
//        for (BleDevice bleDevice : deviceList) {
//            mDeviceAdapter.addDevice(bleDevice);
//        }
//        mDeviceAdapter.notifyDataSetChanged();
//    }

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
//            Toast.makeText(this, "App has obtained the required permissions", Toast.LENGTH_SHORT).show();
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
//    private void bleSetScanRule() {
//        // UUID
//        String[] uuids;
//        String str_uuid = setUuidEdit.getText().toString();
//        if (TextUtils.isEmpty(str_uuid)) {
//            uuids = null;
//        } else {
//            uuids = str_uuid.split(",");
//        }
//        UUID[] serviceUuids = null;
//        if (uuids != null && uuids.length > 0) {
//            serviceUuids = new UUID[uuids.length];
//            for (int i = 0; i < uuids.length; i++) {
//                String name = uuids[i];
//                String[] components = name.split("-");
//                if (components.length != 5) {
//                    serviceUuids[i] = null;
//                } else {
//                    serviceUuids[i] = UUID.fromString(uuids[i]);
//                }
//            }
//        }
//
//        // Name
//        String[] names;
//        String str_name = setNameEdit.getText().toString();
//        if (TextUtils.isEmpty(str_name)) {
//            names = null;
//        } else {
//            names = str_name.split(",");
//        }
//
//        // MAC
//        String mac = setMacEdit.getText().toString();
//
//        // AutoConnect
//        boolean isAutoConnect = setAutoConnectSw.isChecked();
//
//        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(serviceUuids)      // Only scan the devices of the specified service, optional
//                .setDeviceName(true, names)    // Only scan devices with specified broadcast name, optional
//                .setDeviceMac(mac)                  // Only scan devices of specified mac, optional
//                .setAutoConnect(isAutoConnect)      // AutoConnect parameter when connecting, optional, default false
//                .setScanTimeOut(10000)              // Scan timeout time, optional, default 10 seconds
//                .build();
//        BleManager.getInstance().initScanRule(scanRuleConfig);
//    }

    /**
     * Start BLE scan
     */
//    private void bleStartScan() {
//        BleManager.getInstance().scan(new BleScanCallback() {
//            @Override
//            public void onScanStarted(boolean success) {
//
//                bleDeviceList.clear();
//
//                mDeviceAdapter.clear();
//                mDeviceAdapter.notifyDataSetChanged();
//
//                recyclerView.removeAllViews();
//
////                settingLayout.setVisibility(View.GONE);
//
//                swipeRefreshLayout.setVisibility(View.VISIBLE);
//                swipeRefreshLayout.setRefreshing(true);
//
//                scanMenuItem.setTitle(getString(R.string.stop_scan));
//
//                bleScanStatus = BLE_SCAN_STATUS;
//            }
//
//            @Override
//            public void onLeScan(BleDevice bleDevice) {
//                super.onLeScan(bleDevice);
//            }
//
//            @Override
//            public void onScanning(BleDevice bleDevice) {
//                if(bleDevice.getName() !=null && bleDevice.getName().contains("CRISTAL")){
//
//                    System.out.println("this is from manin --> "+bleDevice.getName());
//
//                    mDeviceAdapter.addDevice(bleDevice);
//                    mDeviceAdapter.notifyDataSetChanged();
//
//                }
//
//            }
//
//            @Override
//            public void onScanFinished(List<BleDevice> scanResultList) {
//                scanMenuItem.setTitle(getString(R.string.start_scan));
//
//                swipeRefreshLayout.setRefreshing(false);
//
//                bleScanStatus = BLE_STOP_SCAN_STATUS;
//            }
//        });
//    }

    /**
     * Connect BLE device
     * @param bleDevice
     */
//    public void bleConnect(final BleDevice bleDevice) {
//        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
//            @Override
//            public void onStartConnect() {
//                progressDialog.setMessage("Connecting");  // Set the BLE connection ProgressDialog prompt text
//                progressDialog.show();  // Show BLE connection ProgressDialog
//            }
//
//            @Override
//            public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                scanMenuItem.setTitle(getString(R.string.start_scan));
//
//                progressDialog.dismiss();  // Hide BLE connection ProgressDialog
//
//                bleScanStatus = BLE_SCAN_STATUS;
//
//                Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                progressDialog.dismiss();  // Hide BLE connection ProgressDialog
//
//                mDeviceAdapter.addDevice(bleDevice);
//                mDeviceAdapter.notifyDataSetChanged();
//
//                Intent intent = new Intent(MainActivity.this, OperationActivity.class);
//                intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
//                startActivity(intent);
//            }
//
//            @Override
//            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
//                progressDialog.dismiss();  // Hide BLE connection ProgressDialog
//
//                mDeviceAdapter.removeDevice(bleDevice);
//                mDeviceAdapter.notifyDataSetChanged();
//
//                if (isActiveDisConnected) {
//                    Toast.makeText(MainActivity.this, "You disconnect BLE", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "BLE was disconnected", Toast.LENGTH_SHORT).show();
//
//                    ObserverManager.getInstance().notifyObserver(bleDevice);
//                }
//            }
//        });
//    }

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

                Fragment scanFragment1 = getSupportFragmentManager().findFragmentByTag("ScanFragment");
                if (scanFragment1 != null) {
                    ScanFragment scanFragment = (ScanFragment) scanFragment1;
                    scanFragment.bleSetScanRule();
                    scanFragment.bleStartScan();

                }
            }
        }
    }

    boolean doubleTapToExit = false;
    Handler doubleTapToExitHandler = null;

    @Override
    public void onBackPressed() {

        if (doubleTapToExit) {
            MainActivity.this.finish();
        } else {
            Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_LONG).show();
            doubleTapToExit = true;
            if (doubleTapToExitHandler == null) doubleTapToExitHandler = new Handler(Looper.getMainLooper());
            doubleTapToExitHandler.postDelayed(() -> {
                doubleTapToExit = false;
            }, 2000);
        }
    }
}
