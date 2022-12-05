package com.floppa.stackcabinet.ui

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.models.Cabinet
import com.floppa.stackcabinet.models.Commands
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.ui.shared.CabinetCompose
import com.floppa.stackcabinet.ui.viewmodel.GridViewModel
import com.floppa.stackcabinet.ui.viewmodel.ViewStateGrid


@Composable
fun GridCompose(
    navController: NavHostController,
    viewModel: GridViewModel,
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {

    val graph1 = arrayOf(
        intArrayOf(1),
        intArrayOf(2, 3, 0, 255),
        intArrayOf(0, 4, 5, 1),
        intArrayOf(0, 1, 5, 6),
        intArrayOf(0, 7, 2, 0),
        intArrayOf(2, 7, 8, 3),
        intArrayOf(3, 8, 0, 0),
        intArrayOf(4, 0, 9, 5),
        intArrayOf(0, 0, 5, 9),
        intArrayOf(0, 0, 8, 7))

    val graph2 = arrayOf(
        intArrayOf(1),
        intArrayOf(0, 255, 2, 3),
        intArrayOf(0, 4, 1, 0),
        intArrayOf(1, 4, 0, 0),
        intArrayOf(5, 3, 2, 0),
        intArrayOf(6, 0, 0, 4),
        intArrayOf(0, 5, 0, 7),
        intArrayOf(8, 0, 9, 6),
        intArrayOf(7, 0, 0, 10),
        intArrayOf(11, 0, 0, 7),
        intArrayOf(0, 8, 0, 12),
        intArrayOf(9, 0, 13, 0),
        intArrayOf(0, 14, 0, 10),
        intArrayOf(11, 0, 15, 0),
        intArrayOf(0, 12, 0, 16),
        intArrayOf(0, 17, 0, 13),
        intArrayOf(14, 0, 0, 18),
        intArrayOf(0, 15, 18, 0),
        intArrayOf(16, 0, 17, 0))

    viewModel.calculateGrid(graph2)



    InitScaffold(viewModel, navController) {
        val uiState by viewModel.uiState.collectAsState()
        Box(
            Modifier
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, gestureZoom, _ ->
                        val oldScale = uiState.currentZoom
                        val newScale = uiState.currentZoom * gestureZoom
                        val newOffset =
                            (uiState.currentOffset + (centroid / oldScale)) - ((centroid / newScale) + (pan / oldScale))
                        viewModel.updateUiState(newOffset, newScale)
                    }
                }
                .graphicsLayer {
                    translationX = - uiState.currentOffset.x * uiState.currentZoom
                    translationY = - uiState.currentOffset.y * uiState.currentZoom
                    scaleX = uiState.currentZoom
                    scaleY = uiState.currentZoom
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .fillMaxSize(),
            Alignment.Center
        ) {
            when (val result = viewModel.gridViewState.collectAsState().value) {
                ViewStateGrid.Loading -> println("LOADING")
                is ViewStateGrid.Success -> DrawGrid(result.grid)
//                ViewStateGrid.Disconnected -> TODO()
//                is ViewStateGrid.Problem -> TODO()
            }
        }
    }
}

@Composable
fun DrawGrid(grid: List<Cabinet>?) {
    grid?.forEach {
        CabinetCompose(borderColor = it.cabinetColor,
            backgroundColor = it.ledColor,
            x = it.x,
            y = it.y,
            onClick = { println( "Show part: ${it.id}, ${it.isBase}") },
            onLongClink = { println("Edit") },
            onDoubleClick = { println("Open Cabinet") }) {
        }
    }
}


@Composable
fun InitScaffold(
    viewModel: GridViewModel?,
    navController: NavHostController?,
    content: @Composable () -> Unit,
) {
    val currentRoute = navController?.currentBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = {
                        viewModel?.makeCall(Commands.REQUEST_GRID)
                    }) {
                        Icon(Icons.Rounded.Cached, "reload Data")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                BottomNavigationItem(icon = {
                    Screens.Grid.icon?.let { Icon(imageVector = it, "") }
                },
                    label = { Text(text = stringResource(Screens.Grid.labelResourceId)) },
                    selected = (currentRoute == Screens.Grid.route),
                    onClick = {
                        if (currentRoute != Screens.Grid.route) {
                            navController?.navigate(Screens.Grid.route) {
                                popUpTo(Screens.Components.route) { inclusive = true }
                            }
                        }
                    })
                BottomNavigationItem(icon = {
                    Screens.Components.icon?.let { Icon(imageVector = it, "") }
                },
                    label = { Text(text = stringResource(Screens.Components.labelResourceId)) },
                    selected = (currentRoute == Screens.Components.route),
                    onClick = {
                        if (currentRoute != Screens.Components.route) {
                            navController?.navigate(Screens.Components.route) {
                                popUpTo(Screens.Grid.route) { inclusive = true }
                            }
                        }
                    })
            }
        }

    ) { contentPadding ->
        // Screen content
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}


