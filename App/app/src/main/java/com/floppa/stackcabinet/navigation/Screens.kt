package com.floppa.stackcabinet.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.floppa.stackcabinet.R

sealed class Screens(val route: String, @StringRes val labelResourceId: Int, val icon: ImageVector?, ) {
    object Boot  : Screens("boot", R.string.txt_screen_setup, null)
    object Setup : Screens("setup", R.string.txt_screen_boot, null)
    object Main  : Screens("Grid", R.string.txt_screen_grid, Icons.Rounded.Apps)
    object Components : Screens("Components", R.string.txt_screen_component, Icons.Rounded.Menu)
    object Settings : Screens("Settings", R.string.txt_screen_settings, Icons.Rounded.Settings)
}