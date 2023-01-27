package com.floppa.stackcabinet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.models.Cabinet
import com.floppa.stackcabinet.models.Commands
import com.floppa.stackcabinet.models.ComponentsTypes
import com.floppa.stackcabinet.models.cabinets.Square
import com.floppa.stackcabinet.models.states.Connection.*
import com.floppa.stackcabinet.navigation.Screens
import com.floppa.stackcabinet.ui.shared.CabinetCompose
import com.floppa.stackcabinet.ui.shared.CenterElement
import com.floppa.stackcabinet.ui.shared.Loading
import com.floppa.stackcabinet.ui.viewmodel.GridViewModel
import com.floppa.stackcabinet.ui.viewmodel.ViewStateGrid.*
import com.floppa.stackcabinet.ui.viewmodel.ViewStateScreen
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


sealed class Dialog {
    object Closed : Dialog()
    object ColorPicker : Dialog()
    object ComponentPicker : Dialog()
}

@Composable
fun GridCompose(
    viewModel: GridViewModel,
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    DisposableEffect(lifeCycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for sending analytics events
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.startConnection()
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.stopConnection()
                else -> {}
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SetupScaffold(viewModel) {
        when (viewModel.viewStateScreen.collectAsState().value) {
            // Check which screen / Grid or Components
            ViewStateScreen.Grid -> {
                // Check connection state, Connected, connecting or disconnected
                when (viewModel.connectionState.collectAsState().value) {
                    CONNECTING -> Loading(R.string.txt_connecting)
                    CONNECTED -> {
                        // Make the call to get the grid from the EPS32
                        LaunchedEffect(Unit) {
                            viewModel.makeCall(Commands().requestGrid())
                        }
                        when (val result = viewModel.gridViewState.collectAsState().value) {
                            // Show result after getting and calculating the grid
                            Loading -> Loading(R.string.txt_loading)
                            is Success -> DrawGrid(result.grid, viewModel)
                            is Problem -> TODO()
                        }
                    }
                    DISCONNECTED -> CenterElement {
                        Text(text = "Disconnected")
                    }
                    ERROR -> CenterElement {
                        Text(text = "Could not connect to StackCabinet, please try again",
                            textAlign = TextAlign.Center)
                    }
                    STREAMING -> TODO()
                }
            }
            ViewStateScreen.Components -> {
                ComponentsCompose(viewModel)
            }
        }
    }
}

@Composable
fun DrawGrid(grid: List<Cabinet>?, viewModel: GridViewModel) {

    val showColorWheel = remember { mutableStateOf(false) }
    val cabinetToEdit = remember { mutableStateOf(Square()) }
    val uiState by viewModel.uiState.collectAsState()

    if (showColorWheel.value) {
        DialogChangeSettings(state = showColorWheel, cabinet = cabinetToEdit, viewModel = viewModel)
    }

    Box(
        Modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val oldScale = uiState.currentZoom
                    val newScale = uiState.currentZoom * gestureZoom
                    val newOffset =
                        (uiState.currentOffset + (centroid / oldScale)) - ((centroid / newScale) + (pan / oldScale))
                    viewModel.updateUiStateGrid(newOffset, newScale)
                }
            }
            .graphicsLayer {
                translationX = -uiState.currentOffset.x * uiState.currentZoom
                translationY = -uiState.currentOffset.y * uiState.currentZoom
                scaleX = uiState.currentZoom
                scaleY = uiState.currentZoom
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .fillMaxSize(),
        Alignment.Center
    ) {
        grid?.forEach {
            CabinetCompose(borderColor = it.cabinetColor,
                backgroundColor = it.ledColor,
                x = it.x,
                y = it.y,
                width = it.width,
                height = it.height,
                clickable = !(it.id == 255 || it.id < 1),
                onClick = {
                    println("Show part: ${it.id}")

                },
                onLongClink = {
                    cabinetToEdit.value = it as Square
                    showColorWheel.value = true
                },
                onDoubleClick = {
                    println("Open Cabinet")
                    viewModel.makeCall(Commands().openCabinet(it))
                }) {
                // Compose for inside the Cabinet
            }
        }
    }
}


@Composable
fun SetupScaffold(
    viewModel: GridViewModel,
    content: @Composable () -> Unit,
) {

    val connection = viewModel.connectionState.collectAsState().value
    val screen = viewModel.viewStateScreen.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = {
                        if (connection == DISCONNECTED || connection == ERROR) {
                            viewModel.startConnection()
                        } else {
                            viewModel.makeCall(Commands().requestGrid())
                        }
                    }) {
                        Icon(Icons.Rounded.Cached, "reload Data")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                BottomNavigationItem(icon = {
                    Screens.Main.icon?.let { Icon(imageVector = it, "") }
                },
                    label = { Text(text = stringResource(Screens.Main.labelResourceId)) },
                    selected = (screen == ViewStateScreen.Grid),
                    onClick = {
                        viewModel.setRoute(Screens.Main)
                    })
                BottomNavigationItem(icon = {
                    Screens.Components.icon?.let { Icon(imageVector = it, "") }
                },
                    label = { Text(text = stringResource(Screens.Components.labelResourceId)) },
                    selected = (screen == ViewStateScreen.Components),
                    onClick = {
                        viewModel.setRoute(Screens.Components)
                    })
                BottomNavigationItem(icon = {
                    Screens.Settings.icon?.let { Icon(imageVector = it, "") }
                },
                    label = { Text(text = stringResource(Screens.Settings.labelResourceId)) },
                    selected = (screen == ViewStateScreen.Settings),
                    onClick = {
                        viewModel.setRoute(Screens.Settings)
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


@Composable
fun DialogChangeSettings(
    state: MutableState<Boolean>,
    cabinet: MutableState<Square>,
    viewModel: GridViewModel,
) {
    val controller = rememberColorPickerController()

    val prevLedColor by remember { mutableStateOf(cabinet.value.ledColor) }
    var newLedColor by remember { mutableStateOf(Color.Unspecified) }
    val prevComponent by remember { mutableStateOf(cabinet.value.component) }
    var newComponent by remember { mutableStateOf(ComponentsTypes.RESISTOR) }

    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    when (viewModel.stateDialog.collectAsState().value) {
        Dialog.Closed -> {
            Dialog(onDismissRequest = { state.value = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Text(text = "Select LED color",
                                modifier = Modifier.padding(top = 4.dp, end = 4.dp))
                            Spacer(Modifier.weight(1f))

                            Box(modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cabinet.value.ledColor)
                                .clickable {
                                    viewModel.setStateDialog(Dialog.ColorPicker)
                                })
                        }
                        Row(
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                        ) {
                            Text(text = "Select Component",
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))

                            Spacer(Modifier.weight(1f))

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                    focusManager.clearFocus()
                                },
                            ) {
                                ComponentsTypes.values().forEach { label ->
                                    DropdownMenuItem(onClick = {
                                        newComponent = label
                                        expanded = false
                                        focusManager.clearFocus()
                                    }) {
                                        Text(text = label.nameFull)
                                    }
                                }
                            }
//                            Box(modifier = Modifier
//                                .size(56.dp)
//                                .clip(RoundedCornerShape(8.dp))
//                                .background(cabinet.value.ledColor)
//                                .clickable {
//                                    viewModel.setStateDialog(Dialog.ColorPicker)
//                                })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = {}) {
                                Text(text = "Update")
                            }
                        }
                    }
                }
            }
        }
        Dialog.ColorPicker -> {
            Dialog(onDismissRequest = { state.value = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            HsvColorPicker(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(10.dp),
                                controller = controller,
                                onColorChanged = {
                                    newLedColor = it.color
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = {
                                viewModel.setStateDialog(Dialog.Closed)
                            }) {
                                Text(text = "Set color")
                            }
                        }
                    }
                }
            }
        }
        Dialog.ComponentPicker -> TODO()
    }
}