package com.floppa.stackcabinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.floppa.stackcabinet.navigation.CompanionNavHost
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.ui.theme.StackCabinetCompanionAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackCabinetCompanionAppTheme {
                CompanionApp()
            }
        }
    }
}

@Composable
fun CompanionApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        bottomBar = {
            BottomNav(navController = navController)
        }
    ) { innerPadding ->
        CompanionNavHost(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}

@Composable
fun BottomNav(navController: NavController) {
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val screens = listOf(
            Screens.Home
        )
        screens.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.labelResourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}