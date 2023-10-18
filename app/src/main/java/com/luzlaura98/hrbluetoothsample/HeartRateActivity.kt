package com.luzlaura98.hrbluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


/**
 * Created by Luz on 18/10/2023.
 */
class HeartRateActivity : AppCompatActivity() {

    private lateinit var tvHeartRate : TextView

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.i(TAG, "onServicesDiscovered status=${convertGattStatus(status)}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Servicios descubiertos con éxito, puedes realizar operaciones BLE aquí.
                val heartRateService = gatt.getService(AppUUIDs.HR_SERVICE_UUID)
                val heartRateCharacteristic = heartRateService?.getCharacteristic(AppUUIDs.HR_MEASUREMENT_UUID)

                if (heartRateCharacteristic != null) {
                    // Habilitar notificaciones para la característica de frecuencia cardíaca.
                    gatt.setCharacteristicNotification(heartRateCharacteristic, true)

                    // Configurar el descriptor para habilitar notificaciones.
                    val descriptor =
                        heartRateCharacteristic.getDescriptor(AppUUIDs.CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                } else {
                    Log.e(TAG, "heartRateCharacteristic == null")
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)//3
            Log.i(TAG, "onDescriptorWrite status=${convertGattStatus(status)}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicWrite status=${convertGattStatus(status)}")
        }

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i(TAG,"onConnectionStateChange status=${convertGattStatus(status)} newState=$newState") // need internet traffic?

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "onConnectionStateChange device connected") // need internet traffic
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                Log.e(TAG, "onConnectionStateChange device disconnected")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.i(TAG, "onCharacteristicChanged(gatt, c, v)")

            if (AppUUIDs.HR_MEASUREMENT_UUID == characteristic.uuid) {
                val flag = characteristic.properties
                var format = -1
                if (flag and 0x01 != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16
                    Log.d(TAG, "Heart rate format UINT16.")
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8
                    Log.d(TAG, "Heart rate format UINT8.")
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                tvHeartRate.text = "$heartRate bpm"
            }
        }

        /*@Deprecated("use onCharacteristicChanged(,,)")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.i(TAG, "onCharacteristicChanged(gatt, c)")
            if (AppUUIDs.HR_MEASUREMENT_UUID == characteristic.uuid) {
                val heartRateValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1)
                tvHeartRate.text = "$heartRateValue bpm"
            }
        }*/
    }

    private fun convertGattStatus(status: Int) = when (status) {
        BluetoothGatt.GATT_CONNECTION_CONGESTED -> "BluetoothGatt.GATT_CONNECTION_CONGESTED"
        BluetoothGatt.GATT_FAILURE -> "BluetoothGatt.GATT_FAILURE"
        BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION"
        BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION -> "BluetoothGatt.GATT_INSUFFICIENT_AUTHORIZATION"
        BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION"
        BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH"
        BluetoothGatt.GATT_INVALID_OFFSET -> "BluetoothGatt.GATT_INVALID_OFFSET"
        BluetoothGatt.GATT_READ_NOT_PERMITTED -> "BluetoothGatt.GATT_READ_NOT_PERMITTED"
        BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED"
        BluetoothGatt.GATT_SUCCESS -> "BluetoothGatt.GATT_SUCCESS"
        BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "BluetoothGatt.GATT_WRITE_NOT_PERMITTED"
        else -> "Unknown($status)"
    }

    companion object {
        private const val TAG = "HeartRateActivity"
        private const val EXTRA_DEVICE = "extra_device"
        fun buildIntent(context : Context, device : BluetoothDevice): Intent {
            return Intent(context, HeartRateActivity::class.java)
                .putExtra(EXTRA_DEVICE, device)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)
        tvHeartRate = findViewById(R.id.tvHeartRate)

        val device : BluetoothDevice? = intent?.getParcelableExtra(EXTRA_DEVICE)
        if (device == null){
            Toast.makeText(this, "device == null", Toast.LENGTH_SHORT).show()
            finish()
            return
        }else{
            val bluetoothGatt = device.connectGatt(this, false, gattCallback)
            bluetoothGatt.discoverServices()
        }
    }
}