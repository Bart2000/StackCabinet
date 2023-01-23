package com.floppa.stackcabinet.models.cabinets

import androidx.compose.ui.unit.dp
import com.floppa.stackcabinet.models.Cabinet
import com.floppa.stackcabinet.models.Direction

class Square : Cabinet() {
    private val spacing = 150

    init {
        this.width = 100.dp
        this.height = 100.dp
    }

    override fun calculatePosition(adjacent: Cabinet, gate: Int) {
        val pos = (adjacent.dir?.ordinal?.plus(gate))?.rem(4)
        if (pos != null) {
            val vector: IntArray = vectors[pos]
            val receivingGate: Int = this.adjacent.indexOf(adjacent)
            val direction = (pos + 2 + (4 - receivingGate)) % 4
            this.dir = Direction.values()[direction]
            this.x = adjacent.x + spacing * 2 * vector[0]
            this.y = adjacent.y + spacing * 2 * vector[1]
        }
    }

    override fun toString(): String {

        val builder = StringBuilder()
        builder.append("Square: id: $id | X: $x, Y: $y | neighbors: ")
        adjacent.forEach {
            builder.append("${it?.id} ")
        }
        return builder.toString()
    }
}