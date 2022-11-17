package com.floppa.stackcabinet.ui.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import com.floppa.stackcabinet.repository.BluetoothRepository

class GridViewModel(context: Context): ViewModel() {
    private val repository = BluetoothRepository(context = context)

    fun getPairedBase(): BluetoothDevice? {
        return repository.getPairedBase()
    }
    fun startConnection(){
        repository.startClient(repository.getPairedBase())
    }

    fun stopConnection(){
        repository.stopClient(repository.getPairedBase())
    }

    fun writeData(){
        repository.writeToStream("Hello Back".toByteArray(Charsets.UTF_8))
    }
}