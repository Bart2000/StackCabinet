package com.floppa.stackcabinet.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.floppa.stackcabinet.repository.BluetoothRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


@HiltViewModel
class BootViewModel
@Inject constructor(
    @ApplicationContext appContext: Context,
) : ViewModel() {
    private val repository = BluetoothRepository(context = appContext, handler = null)

    /**
     * @return true/false based on if a StackCabinet base is already paired to the phone
     */
    fun isPaired(): Boolean {
        return repository.isPaired()
    }

    fun getBluetoothState(): Int? {
        return repository.bluetoothState()
    }

    fun enableBluetooth(): Boolean? {
        return repository.enableBluetooth()
    }

    /**
     * Check if the devices had bluetooth
     */
    fun bluetoothAvailable(): Boolean {
        return repository.bluetoothAvailable()
    }
}