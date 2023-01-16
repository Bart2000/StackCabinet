package com.floppa.stackcabinet.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
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
        val viewModel: BootViewModel = hiltViewModel()
        if (viewModel.bluetoothAvailable()) {
            if (viewModel.isPaired()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screens.Main.route) {
                        popUpTo(Screens.Main.route) { inclusive = true }
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
        ShowDialog(
            title = R.string.txt_title_unable_to_run,
            text = R.string.txt_permissions_denied,
            buttonText = R.string.txt_btn_ask_permissions,
            onClick = {
                openSettings(context = context)
            }) {
        }
    }
}

/**
 * Open the application settings
 */
fun openSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    startActivity(context, intent, null)
}

@Composable
fun ShowDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes buttonText: Int,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
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
                            text = stringResource(title),
                            modifier = Modifier.padding(end = 8.dp),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    Text(
                        text = stringResource(text),
                    )
                    Button(onClick = { onClick() }) {
                        Text(text = stringResource(buttonText))
                    }
                }
            }
        }
    }
}