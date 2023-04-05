package com.cristal.ble.ui

import android.app.ProgressDialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.cristal.ble.R
import com.cristal.ble.adapter.DeviceAdapter
import com.cristal.ble.adapter.DeviceAdapter.OnDeviceClickListener
import com.cristal.ble.comm.ObserverManager
import com.cristal.ble.ui.player.OperationActivity


/**
 * A simple [Fragment] subclass.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScanFragment : Fragment() {

    private val TAG = "MainActivity"

    private var mListener: FragmentInteractionListener? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    var mDeviceAdapter: DeviceAdapter? = null
    private val bleDeviceList: ArrayList<BleDevice> = ArrayList()
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(view)
    }

    private fun initUi(view: View) {

        progressDialog = ProgressDialog(context)

        // Pull down to refresh
        swipeRefreshLayout = view.findViewById<View>(R.id.device_swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeRefreshLayout.setVisibility(View.VISIBLE)
        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener { //Pull down to refresh to achieve BLE scanning function

            bleDeviceList.clear()
            mDeviceAdapter?.clear()

            bleSetScanRule()
            bleStartScan()
            System.out.println("--> swipeRefreshLayout.setOnRefreshListener")
            //  for cancelling the scan
        //  BleManager.getInstance().cancelScan()
        })


        recyclerView = view.findViewById<View>(R.id.device_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        mDeviceAdapter = DeviceAdapter(bleDeviceList)
        mDeviceAdapter?.setOnDeviceClickListener(object : OnDeviceClickListener {
            override fun onConnect(bleDevice: BleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan()
                    bleConnect(bleDevice)
                }
                Log.d(TAG, "onConnect: " + bleDevice.name)
            }

            override fun onDetail(bleDevice: BleDevice) {
                // Parse the broadcast packet
                Log.d(TAG, "onDetail: " + bleDevice.name)
            }
        })
        recyclerView.adapter = mDeviceAdapter

        bleSetScanRule();
        bleStartScan();
    }

    /**
     *
     * Show connectable BLE devices
     */
    private fun showConnectedDevice() {
        val deviceList = BleManager.getInstance().allConnectedDevice
        mDeviceAdapter!!.clearConnectedDevice()
        for (bleDevice in deviceList) {
            mDeviceAdapter!!.addDevice(bleDevice)
        }
        mDeviceAdapter!!.notifyDataSetChanged()
    }

    /**
     *
     * Set BLE scanning rules
     */
    public fun bleSetScanRule() {
//        // UUID
//        val uuids: Array<String>?
//        val str_uuid: String = setUuidEdit.getText().toString()
//        uuids = if (TextUtils.isEmpty(str_uuid)) {
//            null
//        } else {
//            str_uuid.split(",").toTypedArray()
//        }
//        var serviceUuids: Array<UUID?>? = null
//        if (uuids != null && uuids.size > 0) {
//            serviceUuids = arrayOfNulls(uuids.size)
//            for (i in uuids.indices) {
//                val name = uuids[i]
//                val components = name.split("-").toTypedArray()
//                if (components.size != 5) {
//                    serviceUuids[i] = null
//                } else {
//                    serviceUuids[i] = UUID.fromString(uuids[i])
//                }
//            }
//        }
//
//        // Name
//        val names: Array<String>?
//        val str_name: String = setNameEdit.getText().toString()
//        names = if (TextUtils.isEmpty(str_name)) {
//            null
//        } else {
//            str_name.split(",").toTypedArray()
//        }
//
//        // MAC
//        val mac: String = setMacEdit.getText().toString()
//
//        // AutoConnect
//        val isAutoConnect: Boolean = setAutoConnectSw.isChecked()

        val scanRuleConfig = BleScanRuleConfig.Builder()
//            .setServiceUuids(serviceUuids) // Only scan the devices of the specified service, optional
//            .setDeviceName(true, names) // Only scan devices with specified broadcast name, optional
//            .setDeviceMac(mac) // Only scan devices of specified mac, optional
//            .setAutoConnect(isAutoConnect) // AutoConnect parameter when connecting, optional, default false
            .setScanTimeOut(10000) // Scan timeout time, optional, default 10 seconds
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }
    //    /**
    //     *
    //     * Set BLE scanning rules
    //     */
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
     *
     * name: null  mac: 4C:7E:F8:93:D8:24  Rssi: -94  scanRecord: 02 01 1a 02 0a 07 0b ff 4c 00 10 06 09 1d 50 9e 33 68 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     * name: boAt Rockerz 255 Pro+-GFP  mac: 40:00:01:D3:DB:C4  Rssi: -98  scanRecord: 02 0a f5 10 16 2c fe 00 90 20 00 02 80 80 80 02 00 a0 11 4f 1a 09 62 6f 41 74 20 52 6f 63 6b 65 72 7a 20 32 35 35 20 50 72 6f 2b 2d 47 46 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
     */
    public fun bleStartScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                bleDeviceList.clear()
                mDeviceAdapter?.clear()
                mDeviceAdapter?.notifyDataSetChanged()
                recyclerView.removeAllViews()

//                settingLayout.setVisibility(View.GONE);
                swipeRefreshLayout.visibility = View.VISIBLE
                swipeRefreshLayout.isRefreshing = true
//                scanMenuItem.setTitle(getString(R.string.stop_scan))
//                bleScanStatus = MainActivity.BLE_SCAN_STATUS
            }

            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                if (bleDevice.name != null && bleDevice.name.contains("CRISTAL")) {
                    println("this is from manin --> " + bleDevice.name)
                    mDeviceAdapter?.addDevice(bleDevice)
//                    mDeviceAdapter?.notifyDataSetChanged()
                }
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
//                scanMenuItem.setTitle(getString(R.string.start_scan))
                swipeRefreshLayout.isRefreshing = false
//                bleScanStatus = MainActivity.BLE_STOP_SCAN_STATUS
            }
        })
    }

    //    /**
    //     *
    //     * Set BLE scanning rules
    //     */
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
    //    /**
    //     * Start BLE scan
    //     */
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
    fun bleConnect(bleDevice: BleDevice?) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                progressDialog?.setMessage("Connecting") // Set the BLE connection ProgressDialog prompt text
                progressDialog?.show() // Show BLE connection ProgressDialog
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
//                scanMenuItem.setTitle(getString(R.string.start_scan))
                progressDialog?.dismiss() // Hide BLE connection ProgressDialog
//                bleScanStatus = MainActivity.BLE_SCAN_STATUS
                Toast.makeText(context, "Connect failed", Toast.LENGTH_SHORT).show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                progressDialog?.dismiss() // Hide BLE connection ProgressDialog
                mDeviceAdapter!!.addDevice(bleDevice)
                mDeviceAdapter!!.notifyDataSetChanged()
                val intent = Intent(context, OperationActivity::class.java)
                intent.putExtra(OperationActivity.KEY_DATA, bleDevice)
                startActivity(intent)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int,
            ) {
                progressDialog?.dismiss() // Hide BLE connection ProgressDialog
                mDeviceAdapter!!.removeDevice(bleDevice)
                mDeviceAdapter!!.notifyDataSetChanged()
                if (isActiveDisConnected) {
                    Toast.makeText(context, "You disconnect BLE", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "BLE was disconnected", Toast.LENGTH_SHORT).show()
                    ObserverManager.getInstance().notifyObserver(bleDevice)
                }
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is FragmentInteractionListener) {
            mListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface FragmentInteractionListener {
        fun onRegisterSuccess()
        fun signin()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ScanFragment()
    }
}