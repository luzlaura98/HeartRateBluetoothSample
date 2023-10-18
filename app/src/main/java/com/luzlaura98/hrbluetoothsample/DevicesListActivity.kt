package com.luzlaura98.hrbluetoothsample

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Luz on 17/10/2023.
 */
class DevicesListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DevicesListActivity"
    }

    private val recyclerAdapter by lazy { DevicesListAdapter{bluetoothDevice ->  }}

    private val bluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val receiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                when (action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        Log.i(TAG, BluetoothAdapter.ACTION_STATE_CHANGED)
                        Toast.makeText(
                            this@DevicesListActivity,
                            "ACTION_STATE_CHANGED",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Log.i(TAG, BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                        findViewById<View>(R.id.progressBar).isVisible = true
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.i(TAG, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                        findViewById<View>(R.id.progressBar).isVisible = false
                    }

                    BluetoothDevice.ACTION_FOUND -> {
                        Log.i(TAG, BluetoothDevice.ACTION_FOUND)
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                        if (device != null) {
                            recyclerAdapter.addDevice(device)
                        }
                    }
                }
            }
        }
    }

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

        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        registerReceiver(receiver, intentFilter)
        setupRecyclerView()
        bluetoothAdapter.startDiscovery()
    }

    private fun setupRecyclerView(){
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = recyclerAdapter
        }
    }

    override fun onPause() {
        super.onPause()
        if (bluetoothAdapter?.isDiscovering == true)
            bluetoothAdapter!!.cancelDiscovery()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}