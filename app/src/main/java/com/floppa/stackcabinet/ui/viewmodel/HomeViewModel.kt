package com.floppa.stackcabinet.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.floppa.stackcabinet.repository.BluetoothRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    private val repository = BluetoothRepository(context = context.applicationContext)



    fun checkBt() {
        repository.checkBt()
    }

    fun checkEnable(succes: () -> Unit) {
        if (repository.bluetoothAdapter?.isEnabled == false) {
            succes()
        }
    }

    fun startDiscovery(){
        repository.startDiscovery()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun checkPermissions(listPermissions: MultiplePermissionsState) {
        if (!listPermissions.allPermissionsGranted) {
            listPermissions.launchMultiplePermissionRequest()
        }
    }

    fun checkPermissionGranted(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
}