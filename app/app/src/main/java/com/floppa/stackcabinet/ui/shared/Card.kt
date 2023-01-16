package com.floppa.stackcabinet.ui.shared

import android.annotation.SuppressLint
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.floppa.stackcabinet.models.Component
import com.floppa.stackcabinet.ui.theme.Purple40
import com.floppa.stackcabinet.ui.theme.Purple80
import kotlin.math.roundToInt

const val ANIMATION_DURATION = 500

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun DraggableCard(
    component: Component,
    cardHeight: Dp,
    isRevealed: Boolean,
    cardOffset: Dp,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
) {
    val offsetX = remember { mutableStateOf(0f) }
    val transitionState = remember {
        MutableTransitionState(isRevealed).apply {
            targetState = !isRevealed
        }
    }
    val transition = updateTransition(transitionState, "cardTransition")
    val cardBgColor by transition.animateColor(
        label = "cardBgColorTransition",
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        targetValueByState = {
            if (isRevealed) Purple40 else Purple80
        }
    )
    val offsetTransition by transition.animateFloat(
        label = "cardOffsetTransition",
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        targetValueByState = { if (isRevealed) (cardOffset.value) - offsetX.value else -offsetX.value },

        )
    val cardElevation by transition.animateDp(
        label = "cardElevation",
        transitionSpec = { tween(durationMillis = ANIMATION_DURATION) },
        targetValueByState = { if (isRevealed) 40.dp else 2.dp }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(cardHeight)
            .offset { IntOffset((offsetX.value + offsetTransition).roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    val original = Offset(offsetX.value, 0f)
                    val summed = original + Offset(x = dragAmount, y = 0f)
                    val newValue = Offset(x = summed.x.coerceIn(0f, cardOffset.value), y = 0f)
                    if (newValue.x >= 10) {
                        onExpand()
                        return@detectHorizontalDragGestures
                    } else if (newValue.x <= 0) {
                        onCollapse()
                        return@detectHorizontalDragGestures
                    }
                    if (change.positionChange() != Offset.Zero) change.consume()
                    offsetX.value = newValue.x
                }
            },
        backgroundColor = cardBgColor,
        shape = remember {
            RoundedCornerShape(5.dp)
        },
        elevation = cardElevation,
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            ListItem(
                text = {
                    Text(component.description, fontWeight = FontWeight.Bold)
                },
                secondaryText = { Text(component.type.nameFull) }
            )
        }
    }
}
