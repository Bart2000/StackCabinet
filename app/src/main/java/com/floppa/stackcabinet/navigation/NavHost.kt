package com.floppa.stackcabinet.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.floppa.stackcabinet.ui.HomeCompose


@Composable
fun CompanionNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screens.Home.route) {
            HomeCompose(
                navController = navController,
                viewModel = viewModel())
        }

    }
}


