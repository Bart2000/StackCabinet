package com.floppa.stackcabinet.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.R.string.txt_screen_grid


sealed class Screens(val route: String, @StringRes val labelResourceId: Int, val icon: ImageVector) {
    object Boot  : Screens("boot", R.string.txt_screen_setup, Icons.Rounded.ArrowForward)
    object Setup : Screens("setup", R.string.txt_screen_setup, Icons.Rounded.ArrowForward)
    object Grid  : Screens("grid", txt_screen_grid, Icons.Rounded.Apps)

}