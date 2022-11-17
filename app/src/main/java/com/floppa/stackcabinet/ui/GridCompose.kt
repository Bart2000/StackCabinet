package com.floppa.stackcabinet.ui

import android.app.Activity
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.ui.shared.CenterElement
import com.floppa.stackcabinet.ui.viewmodel.GridViewModel

@Composable
fun GridCompose(
    navController: NavHostController,
    viewModel: GridViewModel,
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    var device by remember { mutableStateOf("") }

    val activity = LocalContext.current as Activity

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    CenterElement {
        Text(text = "Grid Compose")

        Button(onClick = {
            // HERE make Connection
            viewModel.startConnection()
        }) {
            Text(text = "Connect to ESP")
        }
        Button(onClick = {
            // HERE make break active connection
            viewModel.stopConnection()
        }) {
            Text(text = "Disconnect to ESP")
        }
        Button(onClick = {
            // HERE send some sample data
            viewModel.writeData()
        }) {
            Text(text = "Send data to ESP")
        }
    }









}