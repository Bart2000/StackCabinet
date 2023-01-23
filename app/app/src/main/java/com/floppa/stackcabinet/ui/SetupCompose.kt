package com.floppa.stackcabinet.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.hasPermissions
import com.floppa.stackcabinet.models.Constants.name_stackcabinet_base
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.ui.shared.CenterElement
import com.floppa.stackcabinet.ui.viewmodel.SetupViewModel

@SuppressLint("MissingPermission") // We use context.hasPermissions to check
@Composable
fun SetupCompose(
    navController: NavHostController,
    viewModel: SetupViewModel) {
    // Context
    val context = LocalContext.current

    var bounded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var isPairing by remember { mutableStateOf(false) }

    // List all found BluetoothDevices, Only add the correct devices to the list
    SystemBroadcastReceiver(BluetoothDevice.ACTION_FOUND) {

        if (it != null) {

            val device = if (Build.VERSION.SDK_INT >= 33) {
                it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.

            if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {

                Log.d("SystemBroadcastReceiver", "name: ${device?.name}, ID: ${device?.address}")

                if (!device?.name.isNullOrEmpty()) {
                    if (device?.name == name_stackcabinet_base) {
                        viewModel.foundDevice.add(device)
                    }
                }
                if (viewModel.foundDevice.size > 0) {
                    viewModel.stopDiscovery()
                    isSearching = false
                }
            }
        }
    }

    // Incoming pair request, not needed in production
    /*
    SystemBroadcastReceiver(BluetoothDevice.ACTION_PAIRING_REQUEST) {
        if (it != null) {
            val action = it.action
            if (BluetoothDevice.ACTION_PAIRING_REQUEST == action) {
                val device = if (Build.VERSION.SDK_INT >= 33) {
                    it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
              Log.d("ACTION_PAIRING_REQUEST", "Pairing to ${device?.name} with ID: ${device?.address}")
            }
        }
    }
    */

    /**
     *     Receiver is call when a there is a change in bond state,
     *     When BOND_BONDED, a dialog will pop up telling the user a StackCabinet base devices is
     *     paired with the phone, NOT CONNECTED YET
     */
    SystemBroadcastReceiver(BluetoothDevice.ACTION_BOND_STATE_CHANGED) {

        if (it != null) {
            val state = it.extras?.getInt(BluetoothDevice.EXTRA_BOND_STATE)

            if (state == BOND_BONDED) {
                Log.d("ACTION_BOND_STATE_CHANGED", "BOND_BONDED")
                bounded = true
                isPairing = false
            }
        }
    }

    // When the discovery is ended by Android itself
    SystemBroadcastReceiver(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
        isSearching = false
    }


    /**
     * When a device is connected show dialog and navigate to the next screen, [Screens.Main.route].
     * Backstack is popped, no way to go back to the [SetupCompose] screen.
     */
    if (bounded) {
        ShowDialog{
            navController.navigate(Screens.Main.route) {
                popUpTo(0)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Spilt screen into 2 parts (part 1/2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                CenterElement {
                    Button(onClick = {
                        viewModel.foundDevice.clear()
                        viewModel.checkIsEnabled(
                            enable = {
                                viewModel.enableBluetooth()
                            }, enabled = {
                                viewModel.startDiscovery()
                            })
                        isSearching = true
                    }) {
                        Text(text = stringResource(R.string.txt_btn_search_base),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(end = 8.dp))
                            isPairing = false
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colors.surface)
                        }
                    }
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
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                val shape = RoundedCornerShape(10.dp)
                // List of found devices
                viewModel.foundDevice.forEach { device ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .shadow(
                                elevation = 5.dp,
                                shape = shape
                            )
                            .background(color = MaterialTheme.colors.surface, shape = shape)
                            .clickable(onClick = {
                                viewModel.stopDiscovery()
                                @SuppressLint("MissingPermission")
                                isPairing = true
                                if (context.hasPermissions(Manifest.permission.BLUETOOTH_CONNECT)) {
                                    device.createBond()
                                }
                            })
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (context.hasPermissions(Manifest.permission.BLUETOOTH_SCAN)) {
                                Row(modifier = Modifier
                                    .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                                    .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = device.name.toString())
                                    if (isPairing) {
                                        CircularProgressIndicator(modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colors.primarySurface)
                                    }
                                }
                                Row(modifier = Modifier
                                    .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
                                    .fillMaxWidth()) {
                                    Text(text = device.address)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

/**
 * Universal [BroadcastReceiver], is used to register to different receivers
 */
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
fun ShowDialog(setShowDialog: () -> Unit) {
    Dialog(onDismissRequest = {  }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.txt_title_all_setup),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.txt_all_setup)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text(text = "Continue setup", textAlign =  TextAlign.Center)
                        },
                        onClick = { setShowDialog() })
                }
            }
        }
    }
}

/**
 * Simple function to preview [ShowDialog]
 */
@Preview
@Composable
fun ShowDialogPreview(){
    ShowDialog{ }
}


