package com.floppa.stackcabinet.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.ui.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeCompose(
    navController: NavHostController,
    viewModel: HomeViewModel,
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    // Context
    val context = LocalContext.current

    // List of permissions needed for BLT
    val listPermissions = rememberMultiplePermissionsState(permissions =
    listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION

    ))

    // Check Permissions on startup
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    // If the permissions are not granted, ask the user
                    viewModel.checkPermissions(listPermissions)
                }
                else -> {}
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SystemBroadcastReceiver(BluetoothDevice.ACTION_FOUND) {

        if (it != null) {

            val device = if (Build.VERSION.SDK_INT >= 33) {
                it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            if (viewModel.checkPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {

                Log.d("SystemBroadcastReceiver", "name: ${device?.name}, ID: ${device?.address}")

            }
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Spilt screen into 2 parts (part 1/2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column() {
                CenterElement {
                    Button(onClick = { viewModel.startDiscovery() }) {
                        Text(text = stringResource(R.string.txt_btn_bt_scan))
                    }
//                    Button(onClick = { setState(false) }) {
//                        Text(text = stringResource(R.string.txt_btn_bt_off))
//                    }
//                    Button(onClick = { scan() }) {
//                        Text(text = stringResource(R.string.txt_btn_bt_scan))
//                    }
                }
            }
        }
        // Part (2/2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {

                // List of found devices
//                devices.forEach { device ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 10.dp, vertical = 5.dp),
//                        elevation = 10.dp
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(10.dp),
//                            verticalArrangement = Arrangement.spacedBy(10.dp)
//                        ) {
//                            if (ActivityCompat.checkSelfPermission(applicationContext,
//                                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
//                            ) {
//                                Text(text = device.name.toString())
//                            }
//                            Text(text = device.address)
//                        }
//                    }
//
//                }
            }
        }
    }
}

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit,
) {
    // Grab the current context in this part of the UI tree
    val context = LocalContext.current

    // Safely use the latest onSystemEvent lambda passed to the function
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}


@Composable
fun showDialog() {

    val activity = (LocalContext.current as? Activity)

    AlertDialog(onDismissRequest = {

        activity?.finish()
    },
        title = {
            Text(text = "Test")
        },
        buttons = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { println("s") }
                ) {
                    Text("Dismiss")
                }
            }
        })
}


@Composable
fun CenterElement(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        content()
    }
}