package com.floppa.stackcabinet.ui.shared

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

fun Modifier.place(x: Int, y: Int) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x, y)
    }
}


@Preview
@Composable
fun PreviewCabinetCompose() {
    CabinetCompose(borderColor = Color.Magenta,
        backgroundColor = Color.Red,
        x = 10,
        y = 10,
        onClick = { println("click") },
        onLongClink = { println(" Long click") },
        onDoubleClick = { println(" Double click") }) {
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CabinetCompose(
    borderColor: Color,
    backgroundColor: Color,
    x: Int,
    y: Int,
    onClick: () -> Unit,
    onLongClink: () -> Unit,
    onDoubleClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(Modifier
        .place(x, y)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .border(
                    width = 7.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp),
                )
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { onLongClink() },
                    onDoubleClick = { onDoubleClick() }
                )
        ) {
            CenterElement {
                content()
            }
        }

    }
}