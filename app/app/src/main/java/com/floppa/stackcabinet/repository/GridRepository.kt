package com.floppa.stackcabinet.repository

import com.floppa.stackcabinet.database.Resource
import com.floppa.stackcabinet.models.Cabinet
import com.floppa.stackcabinet.models.Direction
import com.floppa.stackcabinet.models.cabinets.Colors
import com.floppa.stackcabinet.models.cabinets.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class GridRepository {
    private val cabinets: ArrayList<Cabinet> = ArrayList()

    fun calculateGrid(graph: Array<IntArray>): Flow<Resource<ArrayList<Cabinet>>> {
        return flow {
            cabinets.clear()
            emit(Resource.Loading())
            // Make a Cabinet object for each index in the IntArray
            graph.forEachIndexed { index, list ->
                val cabinet = Square()
                cabinet.id = index
                cabinet.cabinetColor = Colors.values()[list[list.lastIndex]].color
                cabinets.add(cabinet)
            }
            // Setup the base Cabinet
            val base = cabinets[0]
            base.id = 255
            base.x = 0
            base.y = 0
            base.dir = Direction.RIGHT
            base.isBase = true
            base.visited = true
            base.adjacent.add(getCabinetById(1))

            // Set cabinet nodes
            for (i in 1 until graph.size){
                val nodes = graph[i]
                val c = cabinets[i]

                for (n in 0 until nodes.size - 2){
                    c.adjacent.add(getCabinetById(nodes[n]))
                }
            }
            calculatePositions()
            emit(Resource.Success(cabinets))
        }.flowOn(Dispatchers.Default)
    }

    private fun calculatePositions() {

        cabinets.forEachIndexed { indexCabinets, _ ->
            val cabinet = cabinets[indexCabinets]
            val adjacent = cabinet.adjacent

            adjacent.forEachIndexed { indexAdjacent, _ ->
                val a = adjacent[indexAdjacent]

                if (a != null) {
                    if (!a.visited) {
                        a.calculatePosition(cabinet, indexAdjacent)
                        a.visited = true
                    }
                }
            }

        }
    }

    private fun getCabinetById(id: Int): Cabinet? {
        for (cabinet in cabinets) {
            if (cabinet.id == id) {
                return cabinet
            }
        }
        return null
    }


    override fun toString(): String {
        val builder = StringBuilder("StackCabinet:")
        for (c in cabinets) {
            builder.append("\n")
            builder.append(c.id.toString() + " - " + c.toString())
        }
        return builder.toString()
    }
}
