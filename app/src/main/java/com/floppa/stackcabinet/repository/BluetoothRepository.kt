package com.floppa.stackcabinet.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.util.Log
import com.floppa.stackcabinet.hasPermissions
import com.floppa.stackcabinet.models.Constants.name_stackcabinet_base
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*


@SuppressLint("MissingPermission")
class BluetoothRepository(private val context: Context) {
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val BLUETOOTH_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Check if the devices had bluetooth
     */
    fun bluetoothAvailable(): Boolean {
        return (bluetoothAdapter != null)
    }

    /**
     * Enable bluetooth, if SDK version is (or greater) then [Build.VERSION_CODES.TIRAMISU],
     * this will return false, not allow, else enable bluetooth
     */
    fun enableBluetooth() {
        if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                bluetoothAdapter?.enable()
                bluetoothAdapter?.isDiscovering
            }
        }
    }

    /**
     * Disable bluetooth, if SDK version is (or greater) then [Build.VERSION_CODES.TIRAMISU],
     * this will return false, not allow, else disable bluetooth
     */
    fun disableBluetooth() {
        if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                bluetoothAdapter?.disable()
            }
        }
    }

    /**
     * Start the discovery of new Bluetooth devices
     */
    fun startDiscovery() {
        if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            } else {
                bluetoothAdapter?.startDiscovery()
            }
        }
    }

    /**
     * Stop the discovery of new Bluetooth devices
     */
    fun stopDiscovery() {
        if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }


    suspend fun setupSocket(device: BluetoothDevice) {
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        withContext(defaultDispatcher) {
            var socket: BluetoothSocket? = null
            var out: OutputStream? = null
            try {
                if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
                    socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP)
                    socket.connect();
                    out = socket.outputStream;
                } else {

                }
            } catch (e: IOException) {
                Log.e("BluetoothRepository", "$e")
            }
        }
    }

    /**
     * @return true/false based on if a StackCabinet base is already paired to the phone
     */
    fun isPaired(): Boolean {
        val results = bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        results.forEach {
            println(it.name)
            if (it.name.contains(name_stackcabinet_base)) {
                return true
            }
        }
        return false
    }

}