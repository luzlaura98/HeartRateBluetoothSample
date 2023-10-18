package com.luzlaura98.hrbluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Luz on 17/10/2023.
 */
class DevicesBleListActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "DevicesBleListActivity"
        private const val REQUEST_CONNECT_DEVICE = 7
    }

    private var device: BluetoothDevice? = null
    private val recyclerAdapter by lazy {
        DevicesListAdapter {
            startActivity(HeartRateActivity.buildIntent(this, it))
        }
    }

    private val bluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    // scanCallback /////
    private val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            Log.i(
                TAG,
                "onScanResult. callType=$callbackType, device=${device.name}, uuids=${device.uuids}"
            )
            if (device.type == BluetoothDevice.DEVICE_TYPE_LE || device.type == BluetoothDevice.DEVICE_TYPE_DUAL)
            recyclerAdapter.addDevice(device)
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Log.e(TAG, "onBatchScanResults. size=${results?.size}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "onScanFailed=$errorCode")
        }
    }

    private var gatt: BluetoothGatt? = null
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status != BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG, "gatt status=$status")

            Log.i(TAG, "onConnectionStateChange status = $status, new state= $newState") // need internet traffic
            if (newState == BluetoothProfile.STATE_CONNECTED){
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            // all services discovered
            Log.i(TAG, "onServicesDiscovered")
            val services = gatt?.services?:return
            val heartRateUUID = AppUUIDs.HR_MEASUREMENT_UUID
            for (s in services){
                if (s.getCharacteristic(heartRateUUID) != null){
                    if (device != null)
                        startActivity(HeartRateActivity.buildIntent(this@DevicesBleListActivity, device!!))
                    return
                }
            }
            Log.e(TAG, "Heart Rate not available for this device")
            Toast.makeText(this@DevicesBleListActivity, "Heart Rate not available for this device", Toast.LENGTH_SHORT).show()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.i(TAG, "onCharacteristicRead=$characteristic")
            //gatt.disconnect() ??
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices_list)

        if (bluetoothAdapter == null) {
            Log.e(TAG, "bluetoothAdapter == null")
            Toast.makeText(this, "Bluetooth unsupported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "bluetooth not enabled")
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            return
        }


        setupRecyclerView()
        scan()
    }

    @SuppressLint("MissingPermission")
    private fun scan() {

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER) //SCAN_MODE_LOW_POWER // SCAN_MODE_LOW_LATENCY
            //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            //.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            //.setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            //.setReportDelay(0L)
            .build()

        scanner.startScan(emptyList(), scanSettings, scanCallback)
        Log.d(TAG, "scan started")
    }

    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = recyclerAdapter
        }
    }

    // connection
    @SuppressLint("MissingPermission")
    private fun connectToDevice(bluetoothDevice: BluetoothDevice){
        if (gatt == null){
            device = bluetoothDevice
            scanner.stopScan(scanCallback)
            Handler(Looper.getMainLooper())
                .postDelayed({
                    gatt = bluetoothDevice.connectGatt(this,false, gattCallback)
                }, 3000)
        }
    }
}