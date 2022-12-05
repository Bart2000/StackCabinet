package com.floppa.stackcabinet.ui

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.models.Component
import com.floppa.stackcabinet.models.ComponentsTypes
import com.floppa.stackcabinet.models.ProblemState
import com.floppa.stackcabinet.ui.shared.CenterElement
import com.floppa.stackcabinet.ui.viewmodel.GridViewModel
import com.floppa.stackcabinet.ui.viewmodel.ViewStateListComponents

@Composable
fun ComponentsCompose(navController: NavHostController, viewModel: GridViewModel) {

    val showDialogAdd = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getComponents()
    }

    if (showDialogAdd.value) {
        ShowDialogAddComponent(showDialogAdd) {
            viewModel.addComponent(it)
        }
    }

    val result = viewModel.componentsViewState.collectAsState().value

    InitScaffold(viewModel, navController) {

        when (result) {
            is ViewStateListComponents.Loading -> Loading()
            is ViewStateListComponents.Problem -> ShowProblem(result.exception)
            is ViewStateListComponents.Success -> ListComponents(result.components, viewModel)
        }


        /**
         * FAB to add a new Component via the dialog pop-up
         */
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomEnd),
                onClick = {
                    showDialogAdd.value = true
                }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListComponents(components: List<Component>?, viewModel: GridViewModel) {

    val showDialogEdit = remember { mutableStateOf(false) }
    var itemToEdit by remember {
        mutableStateOf(Component("",
            ComponentsTypes.RESISTOR,
            null,
            null))
    }

    if (showDialogEdit.value) {
        ShowDialogEditComponent(showDialogEdit, itemToEdit) {
            println(it)
            viewModel.updateComponent(it)
        }
    }

//    val cards by viewModel.cards.collectAsStateWithLifecycle()
//    val revealedCardIds by viewModel.revealedCardIdsList.collectAsStateWithLifecycle()

    if (components?.isNotEmpty() == true) {
        LazyColumn {
            items(items = components, itemContent = { item ->
                Box(Modifier.fillMaxWidth()) {
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            when (it) {
                                DismissValue.DismissedToStart -> {
                                    viewModel.removeComponent(item)
                                }
                                DismissValue.DismissedToEnd -> {
                                    itemToEdit = item
                                    showDialogEdit.value = true
                                }
                                else -> {}
                            }
                            true
                        })

                    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                        viewModel.removeComponent(item)
                    }
                    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
                        itemToEdit = item
                        showDialogEdit.value = true
                    }

                    SwipeToDismiss(
                        state = dismissState,
                        modifier = Modifier.padding(vertical = 4.dp),
                        directions = setOf(DismissDirection.StartToEnd,
                            DismissDirection.EndToStart),
                        background = {
                            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                            val color by animateColorAsState(
                                when (dismissState.targetValue) {
                                    DismissValue.Default -> Color.LightGray
                                    DismissValue.DismissedToEnd -> Color.hsl(27.0f, 0.99f, 0.48f)
                                    DismissValue.DismissedToStart -> Color.hsl(3.0f, 0.86f, 0.51f)
                                }
                            )
                            val alignment = when (direction) {
                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                DismissDirection.EndToStart -> Alignment.CenterEnd
                            }
                            val icon = when (direction) {
                                DismissDirection.StartToEnd -> Icons.Default.Edit
                                DismissDirection.EndToStart -> Icons.Default.Delete
                            }
                            val scale by animateFloatAsState(
                                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                            )

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = "",
                                    modifier = Modifier.scale(scale)
                                )
                            }
                        },
                        dismissContent = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                elevation = 10.dp
                            ) {
                                ListItem(
                                    text = {
                                        Text(item.description, fontWeight = FontWeight.Bold)
                                    },
                                    secondaryText = { Text(item.type.nameFull) }
                                )
                            }
                        },
                    )
                }
            })
        }
    }
}


@Composable
fun ShowProblem(exception: ProblemState?) {
    CenterElement {
        Text(text = exception?.name.toString())
    }
}

@Composable
fun Loading() {
    CenterElement {
        Text(text = "Loading")
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun PreviewShowDialogAddComponent() {
    ShowDialogAddComponent(state = mutableStateOf(true)) { }
}

@Composable
fun ShowDialogAddComponent(state: MutableState<Boolean>, addComponent: (Component) -> Unit) {

    var expanded by remember { mutableStateOf(false) }
    var choiceCategory by remember { mutableStateOf(ComponentsTypes.RESISTOR) }
    var name by remember { mutableStateOf("") }
    val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.txt_title_add_component),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = stringResource(R.string.txt_add_component))
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { newText ->
                            name = newText.trimStart { it == '0' }
                        },
                        label = { Text(text = "Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = choiceCategory.nameFull,
                        onValueChange = {},
                        trailingIcon = {
                            Icon(icon, null)
                        },
                        readOnly = true,
                        label = { Text(text = "Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                expanded = it.isFocused
                            }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                            focusManager.clearFocus()
                        },
                    ) {
                        ComponentsTypes.values().forEach { label ->
                            DropdownMenuItem(onClick = {
                                choiceCategory = label
                                expanded = false
                                focusManager.clearFocus()
                            }) {
                                Text(text = label.nameFull)
                            }
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text(text = "Add component to your inventory",
                                textAlign = TextAlign.Center)
                        },
                        onClick = {
                            addComponent(Component(
                                description = name,
                                type = choiceCategory,
                                cabinetId = null,
                                index = null))
                            state.value = false
                        })
                }
            }
        }
    }
}

@Composable
fun ShowDialogEditComponent(
    state: MutableState<Boolean>,
    component: Component,
    update: (Component) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    var choiceCategory by remember { mutableStateOf(component.type) }
    var name by remember { mutableStateOf(component.description) }
    val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.txt_title_edit_component),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = stringResource(R.string.txt_edit_component))
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { newText ->
                            name = newText.trimStart { it == '0' }
                        },
                        label = { Text(text = "Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = choiceCategory.nameFull,
                        onValueChange = {},
                        trailingIcon = {
                            Icon(icon, null)
                        },
                        readOnly = true,
                        label = { Text(text = "Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                expanded = it.isFocused
                            }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                            focusManager.clearFocus()
                        },
                    ) {
                        ComponentsTypes.values().forEach { label ->
                            DropdownMenuItem(onClick = {
                                choiceCategory = label
                                expanded = false
                                focusManager.clearFocus()
                            }) {
                                Text(text = label.nameFull)
                            }
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text(text = "save changes",
                                textAlign = TextAlign.Center)
                        },
                        onClick = {
                            update(Component(
                                description = name,
                                type = choiceCategory,
                                cabinetId = component.cabinetId,
                                index = component.index))
                            state.value = false
                        })
                }
            }
        }
    }
}
