package com.floppa.stackcabinet.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.ui.BootCompose
import com.floppa.stackcabinet.ui.GridCompose
import com.floppa.stackcabinet.ui.SetupCompose
import com.floppa.stackcabinet.ui.viewmodel.SetupViewModel

@Composable
fun CompanionNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = Screens.Boot.route
    ) {

        composable(Screens.Boot.route) {
            Scaffold {
                Column(modifier = Modifier.padding(it)) {
                    BootCompose(
                        navController = navController,
                        context = LocalContext.current)
                }
            }
        }

        composable(Screens.Setup.route) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.txt_topBar_setup)) }
                    )
                }
            ) {
                Column(modifier = Modifier.padding(it)) {
                    SetupCompose(
                        navController = navController,
                        viewModel = SetupViewModel(context))
                }
            }
        }

        composable(Screens.Main.route) {
            Scaffold {
                Column(modifier = Modifier.padding(it)) {
                    GridCompose(
                        viewModel = hiltViewModel())
                }
            }
        }
    }
}


