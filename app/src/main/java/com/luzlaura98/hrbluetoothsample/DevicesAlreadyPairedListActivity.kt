package com.luzlaura98.hrbluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by Luz on 18/10/2023.
 */
class DevicesAlreadyPairedListActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "DevicesAlreadyPairedListActivity"
    }

    private val recyclerAdapter by lazy {
        DevicesListAdapter {
            startActivity(HeartRateActivity.buildIntent(this, it))
        }
    }

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

            /*val bluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(bluetoothIntent, 7)*/
            finish()
            return
        }

        setupRecyclerView()
        // Obtiene una lista de dispositivos emparejados
        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                val deviceName = device.name // Nombre del dispositivo
                val deviceAddress = device.address // Dirección MAC del dispositivo
                Log.i(TAG, "device $deviceName address=$deviceAddress")
                recyclerAdapter.addDevice(device)
            }
        }
    }

    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = recyclerAdapter
        }
    }
}