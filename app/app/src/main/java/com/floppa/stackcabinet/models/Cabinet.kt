package com.floppa.stackcabinet.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

abstract class Cabinet(
    var id: Int = 0,
    var x: Int = 0,
    var y: Int = 0,
    var width: Dp = 0.dp,
    var height: Dp = 0.dp,
    var adjacent: ArrayList<Cabinet?> = ArrayList(),
    var visited: Boolean = false,
    var dir: Direction? = null,
    var isBase: Boolean = false,
    var cabinetColor: Color = Color.Red,
    var component: Component? = null,
    var ledColor: Color = Color.Blue,
    val vectors: List<IntArray> = listOf(intArrayOf(0, 1),
        intArrayOf(1, 0),
        intArrayOf(0, -1),
        intArrayOf(-1, 0)),
) {

    override fun toString(): String {
        val builder = StringBuilder("[")
        for (cabinet in adjacent) {
            builder.append(if (cabinet != null) cabinet.id.toString() + ", " else "0, ")
        }
        builder.append("]")
        return builder.toString()
    }

//    @Composable
//    abstract fun Draw(x: Int, y: Int, cabinet: Cabinet)

    abstract fun calculatePosition(adjacent: Cabinet, gate: Int)

}