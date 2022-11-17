package com.floppa.stackcabinet.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import com.floppa.stackcabinet.hasPermissions
import com.floppa.stackcabinet.models.Constants.name_stackcabinet_base
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*


@SuppressLint("MissingPermission")
class BluetoothRepository(private val context: Context) {
    /**
     * Get the [BluetoothManager] and [BluetoothAdapter]
     */
    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

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

    /**
     * @return true/false based on if a StackCabinet base is already paired to the phone
     */
    fun isPaired(): Boolean {
        devices.forEach {
            if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
                if (it.name.contains(name_stackcabinet_base)) {
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
    fun startClient(device: BluetoothDevice?) {
        Log.d(TAG, "startClient: Started.")
        device?.let { ConnectThread(it) }?.start()
    }

    /**
     * Close the connection that is made with the [ConnectThread]
     */
    fun stopClient(device: BluetoothDevice?) {
        if (device != null) {
            ConnectThread(device).cancel()
        }
    }

    /**
     * Start the [InputStream] and [OutputStream] to the [BluetoothDevice]
     */
    private fun startStream(socket: BluetoothSocket){
        this.socket = socket
        ConnectedThread(socket).start()
    }

    /**
     * Write a [ByteArray] to the [OutputStream] of [ConnectedThread]
     */
    fun writeToStream(bytes: ByteArray){
        socket?.let { ConnectedThread(it) }?.write(bytes)
    }

    /**
     * Close the stream of [ConnectedThread]
     */
    fun closeStream() {
        socket?.let { ConnectedThread(it) }?.cancel()
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
                Log.i("ConnectThread", "connecting to Socket")
                socket.connect()

                startStream(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
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
        private val mmBuffer: ByteArray = ByteArray(256) // mmBuffer store for the stream

        override fun run() {

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                val s = String(mmBuffer, StandardCharsets.UTF_8)
                println(mmBuffer)
                println(s)

                // Send the obtained bytes to the UI activity.
//                val readMsg = handler.obtainMessage(
//                    Companion.MESSAGE_READ, numBytes, -1,
//                    mmBuffer)
//                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

//                // Send a failure message back to the activity.
//                val writeErrorMsg = handler.obtainMessage(Companion.MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("toast", "Couldn't send data to the other device")
//                }
//                writeErrorMsg.data = bundle
//                handler.sendMessage(writeErrorMsg)
//                return
            }

            // Share the sent message with the UI activity.
//            val writtenMsg = handler.obtainMessage(
//                Companion.MESSAGE_WRITE, -1, -1, mmBuffer)
//            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}