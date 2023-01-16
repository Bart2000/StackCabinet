package com.floppa.stackcabinet.ui.shared

import androidx.annotation.StringRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.floppa.stackcabinet.R

@Preview
@Composable
fun PreviewLoading() {
    Loading(R.string.txt_loading)
}

@Preview
@Composable
fun PreviewCabinetCompose() {
    CabinetCompose(borderColor = Color.Red,
        backgroundColor = Color.Blue,
        x = 0,
        y = 0,
        width = 100.dp,
        height = 100.dp,
        onClick = { println("click") },
        onLongClink = { println(" Long click") },
        onDoubleClick = { println(" Double click") }) {
    }
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

fun Modifier.place(x: Int, y: Int) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x, y)
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CabinetCompose(
    borderColor: Color,
    backgroundColor: Color,
    x: Int,
    y: Int,
    width: Dp,
    height: Dp,
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
                .size(width, height)
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

@Composable
fun Loading(@StringRes stringRes: Int) {
    CenterElement {
//        CircularProgressIndicator(modifier = Modifier.size(55.dp))
        DotsPulsing()
        Text(text = stringResource(stringRes))
    }
}

@Composable
fun DotsPulsing() {
    val dotSize = 24.dp // made it bigger for demo
    val delayUnit = 300 // you can change delay to change animation speed
    @Composable
    fun Dot(
        scale: Float
    ) = Spacer(
        Modifier
            .size(dotSize)
            .scale(scale)
            .background(
                color = MaterialTheme.colors.primary,
                shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateScaleWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                0f at delay with LinearEasing
                1f at delay + delayUnit with LinearEasing
                0f at delay + delayUnit * 2
            }
        )
    )

    val scale1 by animateScaleWithDelay(0)
    val scale2 by animateScaleWithDelay(delayUnit)
    val scale3 by animateScaleWithDelay(delayUnit * 2)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val spaceSize = 2.dp

        Dot(scale1)
        Spacer(Modifier.width(spaceSize))
        Dot(scale2)
        Spacer(Modifier.width(spaceSize))
        Dot(scale3)
    }
}