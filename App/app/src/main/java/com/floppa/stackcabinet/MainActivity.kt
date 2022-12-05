package com.floppa.stackcabinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.floppa.stackcabinet.navigation.CompanionNavHost
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
    CompanionNavHost()
}
