package com.floppa.stackcabinet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.ui.viewmodel.HomeViewModel

@Composable
fun HomeCompose(navController: NavHostController, viewModel: HomeViewModel) {
    CenterElement {
        Button(onClick = { connectBlt() }) {
            Text(text = "Connect")
        }
    }
}

fun connectBlt() {
    TODO("Not yet implemented")
}


@Composable
fun CenterElement(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        content()
    }
}