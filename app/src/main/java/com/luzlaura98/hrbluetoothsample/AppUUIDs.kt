package com.luzlaura98.hrbluetoothsample

import java.util.UUID

/**
 * Created by Luz on 18/10/2023.
 */
class AppUUIDs {
    companion object{
        val HR_MEASUREMENT_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") //HEART_RATE_MEASUREMENT_UUID
        val HR_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")

        //se utiliza para identificar el descriptor que habilita o deshabilita las notificaciones o indicaciones en una característica BLE
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}