package com.floppa.stackcabinet.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.floppa.stackcabinet.repository.BluetoothRepository

class BootViewModel(context: Context) : ViewModel() {
    private val repository = BluetoothRepository(context = context)

    /**
     * @return true/false based on if a StackCabinet base is already paired to the phone
     */
    fun isPaired(): Boolean {
        return repository.isPaired()
    }

    /**
     * Check if the devices had bluetooth
     */
    fun bluetoothAvailable(): Boolean {
        return repository.bluetoothAvailable()
    }
}