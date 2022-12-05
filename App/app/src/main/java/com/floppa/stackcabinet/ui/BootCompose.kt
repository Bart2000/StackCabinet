package com.floppa.stackcabinet.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.ui.viewmodel.BootViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BootCompose(navController: NavHostController, context: Context) {

    val activity = (LocalContext.current as? Activity)


    // List of permissions needed for BLT, needs more for above Android 11
    val listPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(permissions =
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

        ))
    } else {
        rememberMultiplePermissionsState(permissions =
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

        ))
    }

    /**
     * Check if the app has all the needed permissions on start up
     * Called from non-composable scope otherwise, this will result in an IllegalStateException.
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, effect = {
        val eventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    listPermissions.launchMultiplePermissionRequest()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    })

    /**
     * Check if the devices has
     */
    if (listPermissions.allPermissionsGranted) {
        val viewModel = BootViewModel(context = context)
        if (viewModel.bluetoothAvailable()) {
            if (viewModel.isPaired()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screens.Grid.route) {
                        popUpTo(Screens.Grid.route) { inclusive = true }
                    }
                }
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screens.Setup.route) {
                        popUpTo(Screens.Setup.route) { inclusive = true }
                    }
                }
            }
        }
    } else {
        ShowDialogTargetNotSupported {
            activity?.finish()
        }
    }
}

@Composable
fun ShowDialogTargetNotSupported(setShowDialog: (Boolean) -> Unit) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
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
                            text = stringResource(R.string.txt_title_unable_to_run),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "",
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable { setShowDialog(false) }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    Text(
                        text = stringResource(R.string.txt_unable_to_run),
                    )
                }
            }
        }
    }
}