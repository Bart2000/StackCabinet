package com.floppa.stackcabinet.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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

    CenterElement {
        Text(text = "Grid Compose")
    }

}