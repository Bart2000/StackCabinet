package com.floppa.stackcabinet.ui

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.floppa.stackcabinet.R
import com.floppa.stackcabinet.models.Component
import com.floppa.stackcabinet.models.ComponentsTypes
import com.floppa.stackcabinet.models.ProblemState
import com.floppa.stackcabinet.ui.shared.ActionsRow
import com.floppa.stackcabinet.ui.shared.CenterElement
import com.floppa.stackcabinet.ui.shared.DraggableCard
import com.floppa.stackcabinet.ui.shared.Loading
import com.floppa.stackcabinet.ui.viewmodel.GridViewModel
import com.floppa.stackcabinet.ui.viewmodel.ViewStateComponents

@Composable
fun ComponentsCompose(viewModel: GridViewModel) {

    val showDialogAdd = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getComponents()
    }

    if (showDialogAdd.value) {
        DialogComponent(
            title = R.string.txt_title_add_component,
            text = R.string.txt_add_component,
            textButton = R.string.txt_add_component_btn,
            componentToEdit = null,
            state = showDialogAdd) {
            showDialogAdd.value = false
            viewModel.addComponent(it)
        }
    }

    when (val result = viewModel.viewStateComponents.collectAsState().value) {
        is ViewStateComponents.Loading -> Loading(R.string.txt_loading)
        is ViewStateComponents.Problem -> ShowProblem(result.exception)
        is ViewStateComponents.Success -> ListComponents(result.components, viewModel)
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
        DialogComponent(
            title = R.string.txt_title_edit_component,
            text = R.string.txt_edit_component,
            textButton = R.string.txt_edit_component_btn,
            componentToEdit = itemToEdit,
            state = showDialogEdit) {
            showDialogEdit.value = false
            viewModel.updateComponent(it)
        }
    }

    val revealedCardIds by viewModel.revealedCardIdsList.collectAsState()

    if (components?.isNotEmpty() == true) {
        LazyColumn {
            items(items = components, itemContent = { item ->
                Box(Modifier.fillMaxWidth()) {
                    ActionsRow(
                        actionIconSize = 56.dp,
                        onDelete = {
                            viewModel.removeComponent(item)
                        },
                        onEdit = {
                            itemToEdit = item
                            showDialogEdit.value = true
                        },
                    )
                    DraggableCard(
                        component = item,
                        isRevealed = revealedCardIds.contains(item.index),
                        cardHeight = 72.dp,
                        cardOffset = 320.dp,
                        onExpand = { item.index?.let { viewModel.onItemExpanded(it) } },
                        onCollapse = { item.index?.let { viewModel.onItemCollapsed(it) } },
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

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun PreviewShowDialogAddComponent() {
    DialogComponent(
        title = R.string.txt_title_add_component,
        text = R.string.txt_add_component,
        textButton = R.string.txt_edit_component_btn,
        state = mutableStateOf(true),
        componentToEdit = null,
    ) { }
}

@Composable
fun DialogComponent(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes textButton: Int,
    state: MutableState<Boolean>,
    componentToEdit: Component?,
    onClick: (Component) -> Unit,
    ) {

    var expanded by remember { mutableStateOf(false) }
    var choiceCategory by remember {
        mutableStateOf(componentToEdit?.type ?: ComponentsTypes.RESISTOR)
    }

    val component by remember {
        mutableStateOf(componentToEdit ?: Component("", ComponentsTypes.RESISTOR, null, null))
    }
    val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
    val focusManager = LocalFocusManager.current


    Dialog(onDismissRequest = { state.value = false }) {
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
                            text = stringResource(title),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = stringResource(text))
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = component.description,
                        onValueChange = { newText ->
                            component.description =  newText.trimStart { it == '0' }
                        },
                        label = { Text(text = "Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = choiceCategory.nameFull,
                        onValueChange = { component.type = choiceCategory },
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
                            Text(text = stringResource(textButton),
                                textAlign = TextAlign.Center)
                        },
                        onClick = { onClick(component) })
                }
            }
        }
    }
}