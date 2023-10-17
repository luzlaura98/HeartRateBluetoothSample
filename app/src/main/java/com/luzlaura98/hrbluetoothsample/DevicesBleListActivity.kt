package com.luzlaura98.hrbluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID


/**
 * Created by Luz on 17/10/2023.
 */

/*
* public static class Characteristic {
    final static public UUID HEART_RATE_MEASUREMENT   = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    final static public UUID CSC_MEASUREMENT          = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");
    final static public UUID MANUFACTURER_STRING      = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    final static public UUID MODEL_NUMBER_STRING      = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    final static public UUID APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    final static public UUID BODY_SENSOR_LOCATION     = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
    final static public UUID BATTERY_LEVEL            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    final static public UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
* */
class DevicesBleListActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "DevicesBleListActivity"
        private const val HEART_RATE_MEASUREMENT_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
    }

    private val recyclerAdapter by lazy { DevicesListAdapter() }

    private val bluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
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

        val scanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        // scanCallback /////
        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device: BluetoothDevice = result.device
                // ...do whatever you want with this found device
                Log.i(TAG,"onScanResult. callType=$callbackType, device=${device.name}, uuids=${device.uuids}" )
                recyclerAdapter.addDevice(device)
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                Log.e(TAG, "onBatchScanResults. size=${results?.size}")
                // Ignore for now
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "onScanFailed=$errorCode")
                // Ignore for now
            }
        }

        // filters /////
        val serviceUUID = UUID.fromString(HEART_RATE_MEASUREMENT_UUID) // 0x2A37
        val serviceUUIDs = arrayOf(serviceUUID)
        var filters: MutableList<ScanFilter?>? = null
        filters = ArrayList()
        /*for (serviceUUID in serviceUUIDs) {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(serviceUUID))
                .build()
            filters.add(filter)
        }*/

        // scanSettings /////
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build()

        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback)
            Log.d(TAG, "scan started")
        } else {
            Log.e(TAG, "could not get scanner object")
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = recyclerAdapter
        }
    }
}