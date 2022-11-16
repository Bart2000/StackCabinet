package com.floppa.stackcabinet.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.floppa.stackcabinet.repository.BluetoothRepository


class SetupViewModel(context: Context) : ViewModel() {
    private val repository = BluetoothRepository(context = context)


    /**
     * List of found StackCabinet Bases
     */
    var foundDevice = mutableStateListOf<BluetoothDevice>()


    /**
     * @param enable lambda callback when bluetooth is not enabled
     * @param enabled lambda callback when bluetooth is enabled
     */
    fun checkEnable(enable: () -> Unit, enabled: () -> Unit) {
        if (repository.bluetoothAdapter?.isEnabled == false) {
            enable()
        } else {
            enabled()
        }
    }

    /**
     * Start the discovery of new Bluetooth devices
     */
    fun startDiscovery() {
        repository.startDiscovery()
    }

    /**
     * Stop the discovery of new Bluetooth devices
     */
    fun stopDiscovery() {
        repository.stopDiscovery()
    }

    fun enableBluetooth() {
        repository.enableBluetooth()
    }

    fun disableBluetooth() {
        repository.disableBluetooth()
    }


    suspend fun setupSocket(device: BluetoothDevice) {
        repository.setupSocket(device)
    }

}