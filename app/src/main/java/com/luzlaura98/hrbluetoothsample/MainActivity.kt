package com.luzlaura98.hrbluetoothsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Luz on 17/10/2023.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_PERMISSION_FOR_ALL_DEVICES = 1
        private const val REQUEST_PERMISSION_FOR_BLE_DEVICES = 2
        private const val REQUEST_PERMISSION_PAIRED_DEVICES = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnSearchAllDevices).setOnClickListener {
            onClickConnect()
        }

        findViewById<View>(R.id.btnSearchBleDevices).setOnClickListener {
            onClickSearchBleDevices()
        }

        findViewById<View>(R.id.btnPairedDevices).setOnClickListener {
            onClickPairedDevices()
        }

        /*
        * BluetoothLeScanner
Provided by the BluetoothAdapter class, this class allows us to start a BLE scan.

Note: ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION is required for BLE scans starting from Android M (6.0) and above, whereas ACCESS_FINE_LOCATION is required for Android 10 and above.
        * */
    }

    private fun onClickSearchBleDevices(){
        requestBluetoothPermissions(REQUEST_PERMISSION_FOR_BLE_DEVICES)
    }

    private fun onClickPairedDevices(){
        requestBluetoothPermissions(REQUEST_PERMISSION_PAIRED_DEVICES)
    }

    private fun onClickConnect() {
        requestBluetoothPermissions(REQUEST_PERMISSION_FOR_ALL_DEVICES)
    }

    private fun requestBluetoothPermissions(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ), requestCode
                )
            else
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_FOR_ALL_DEVICES ||
            requestCode == REQUEST_PERMISSION_FOR_BLE_DEVICES ||
            requestCode == REQUEST_PERMISSION_PAIRED_DEVICES) {

            for (index in 0..grantResults.lastIndex) {
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    Log.w(TAG, "No sufficient permissions")
                    Toast.makeText(this, "No sufficient permissions", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Log.d(TAG, "Needed permissions are granted")
            val intent = if (requestCode == REQUEST_PERMISSION_FOR_ALL_DEVICES)
                Intent(this, DevicesListActivity::class.java)
            else if (requestCode == REQUEST_PERMISSION_FOR_BLE_DEVICES)
                Intent(this, DevicesBleListActivity::class.java)
            else
                Intent(this, DevicesAlreadyPairedListActivity::class.java)

            startActivity(intent)
            return
        }
    }

}