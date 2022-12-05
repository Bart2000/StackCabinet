package com.floppa.stackcabinet.models.cabinets

import com.floppa.stackcabinet.models.Cabinet
import com.floppa.stackcabinet.models.Direction

class Square : Cabinet() {
    private val WIDTH = 150

    override fun calculatePosition(cabinet: Cabinet, gate: Int) {
        val pos = (cabinet.dir?.ordinal?.plus(gate))?.rem(4)
        if (pos != null) {
            val vector: IntArray = vectors[pos]
            val receivingGate: Int = adjacent?.indexOf(cabinet) ?: 0
            val direction = (pos + 2 + (4 - receivingGate)) % 4
            this.dir = Direction.values()[direction]
            this.x = cabinet.x + WIDTH * 2 * vector[0]
            this.y = cabinet.y - WIDTH * 2 * vector[1]
        }
    }

    override fun toString(): String {

        val builder = StringBuilder()
        builder.append("Square: id: $id\n")
        adjacent?.forEach {
            builder.append(it?.id)
        }
        return builder.toString()
    }
}