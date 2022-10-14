package com.floppa.stackcabinet.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import com.floppa.stackcabinet.R


sealed class Screens(val route: String, @StringRes val labelResourceId: Int, val icon: ImageVector) {
    object Home : Screens("home", R.string.home, Icons.Rounded.ArrowForward)

}