package com.luzlaura98.hrbluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Luz on 17/10/2023.
 */
class DevicesListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<BluetoothDevice>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DeviceViewHolder).onBind(items[position])
    }

    fun addDevice(item: BluetoothDevice) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    private class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView as TextView

        @SuppressLint("MissingPermission")
        fun onBind(item: BluetoothDevice) {
            textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                "${item.name} / alias = ${item.alias} \ntype = ${item.typeName} / address = ${item.address}"
            else
                "${item.name}\ntype = ${item.typeName} / address = ${item.address}"
        }

        val BluetoothDevice.typeName: String?
            @SuppressLint("MissingPermission")
            get() = when (type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "classic"
                BluetoothDevice.DEVICE_TYPE_LE -> "le"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "dual"
                BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "unknown"
                else -> null
            }
    }
}