package com.floppa.stackcabinet.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import com.floppa.stackcabinet.hasPermissions
import com.floppa.stackcabinet.models.Constants.name_stackcabinet_base
import com.floppa.stackcabinet.models.states.Connection
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*

const val MESSAGE_RECEIVED: Int = 0
const val IS_CONNECTED: Int = 1
const val MESSAGE_WRITE: Int = 2
const val MESSAGE_TOAST: Int = 3

@SuppressLint("MissingPermission")
class BluetoothRepository(private val context: Context, private val handler: Handler?) {
    /**
     * Get the [BluetoothManager] and [BluetoothAdapter]
     */
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    var isConnected = Connection.DISCONNECTED
    var isStreaming = Connection.DISCONNECTED
    var paired = 0

    /**
     * Get a [Set] of paired devices ([BluetoothDevice]) to the phone
     */
    private val devices = bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>

    private var socket: BluetoothSocket? = null

    private val bluetoothSppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Check if the devices had bluetooth
     */
    fun bluetoothAvailable(): Boolean {
        return (bluetoothAdapter != null)
    }

    fun bluetoothState(): Int? {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.state
        }
        return null
    }

    /**
     * Enable bluetooth, if SDK version is (or greater) then [Build.VERSION_CODES.TIRAMISU],
     * this will return false, not allow, else enable bluetooth
     */
    fun enableBluetooth(): Boolean? {
        if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
               return bluetoothAdapter?.enable()
        }
        return null
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

    /**
     * @return true/false based on if a StackCabinet base is already paired to the phone
     */
    fun isPaired(): Boolean {
        devices.forEach {
            if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (it.name.contains(name_stackcabinet_base)) {
                    paired = 1
                    return true
                }
            }
        }
        return false
    }

    /**
     * Look for a paired devices that contains the correct name
     * @return if found [BluetoothDevice], else null
     */
    fun getPairedBase(): BluetoothDevice? {
        devices.forEach {
            if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (it.name.contains(name_stackcabinet_base)) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * open a connection with the [ConnectThread] and run the Thread
     */
    fun startConnection(device: BluetoothDevice?) {
        Log.d("startConnection", "Started.")
        device?.let { ConnectThread(it) }?.start()
    }

    /**
     * Close the connection that is made with the [ConnectThread]
     */
    fun stopConnection(device: BluetoothDevice?) {
        Log.d("stopConnection", "Stopping.")
        if (device != null) {
            closeStream()
            ConnectThread(device).cancel()

        }
    }

    /**
     * Start the [InputStream] and [OutputStream] to the [BluetoothDevice]
     * @param socket the [BluetoothSocket] that will be opened, is function is call by [ConnectThread]
     * When the connection is made with the device.
     */
    private fun startStream(socket: BluetoothSocket) {
        this.socket = socket
        ConnectedThread(socket).start()
    }

    /**
     * Close the stream of [ConnectedThread]
     */
    private fun closeStream() {
        socket?.let {
            ConnectedThread(it).cancel()
        }
    }

    /**
     * Write a [ByteArray] to the [OutputStream] of [ConnectedThread]
     */
    fun writeToStream(bytes: ByteArray) {
        socket?.let {
            ConnectedThread(it).write(bytes)
        }
    }

    /**
     * Inner class to make a connection with the StackCabinet Base.
     * When connection is established, [ConnectedThread] is started.
     * Using the [BluetoothDevice], a [BluetoothSocket] is created, work associated with
     * the connection is done in a separate thread.
     */
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(bluetoothSppUuid)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                    startStream(socket)
                    isConnected = Connection.CONNECTED
                    val readMsg = handler?.obtainMessage(
                        IS_CONNECTED, 1, isConnected.ordinal)
                    readMsg?.sendToTarget()
                } catch (e: Exception) {
                    isConnected = Connection.ERROR
                    val readMsg = handler?.obtainMessage(
                        IS_CONNECTED, 1, isConnected.ordinal)
                    readMsg?.sendToTarget()
                    Log.e("ConnectThread", e.toString())
                }
            }
        }
        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
                isConnected = Connection.DISCONNECTED
                val readMsg = handler?.obtainMessage(
                    IS_CONNECTED, 1, isConnected.ordinal)
                readMsg?.sendToTarget()
            } catch (e: IOException) {
                Log.e("ConnectThread", "Could not close the client socket", e)
            }
        }
    }

    /**
     * Inner class to make a [InputStream] and [OutputStream] between the StackCabinet base and
     * the phone itself.
     * When the Thread is started a while loops keeps checking for incoming data
     * With [write] Data is written to the [BluetoothSocket]
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private var mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()
            isStreaming = Connection.STREAMING
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)

                } catch (e: IOException) {
                    break
                }

                val results = String(mmBuffer, StandardCharsets.UTF_8).take(numBytes).split("|")
                // Send the obtained bytes to the UI activity.
                val readMsg = handler?.obtainMessage(
                    MESSAGE_RECEIVED, numBytes, -1,
                    results)
                readMsg?.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("ConnectedThread", "Error occurred when sending data", e)
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                isStreaming = Connection.DISCONNECTED
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("ConnectedThread", "Could not close the connect socket", e)
            }
        }
    }
}