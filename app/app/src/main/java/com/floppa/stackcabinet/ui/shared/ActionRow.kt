package com.floppa.stackcabinet.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ActionsRow(
    actionIconSize: Dp,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        IconButton(
            modifier = Modifier.size(56.dp, actionIconSize),
            onClick = onDelete,
            content = {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    tint = Color.hsl(0.04f, 0.80f, 0.57f),
                    contentDescription = "delete action",
                )
            }
        )
        IconButton(
            modifier = Modifier.size(56.dp, actionIconSize),
            onClick = onEdit,
            content = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    tint = Color.hsv(35f, 0.97f, 0.96f, 1f),
                    contentDescription = "edit action",
                )
            },
        )
    }
}