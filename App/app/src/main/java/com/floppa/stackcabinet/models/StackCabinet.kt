package com.floppa.stackcabinet.models

import com.floppa.stackcabinet.database.Resource
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
            graph.forEachIndexed { index, _ ->
                val cabinet = Square()
                cabinet.id = index
                cabinets.add(cabinet)
            }
            // Setup the base Cabinet
            val cabinets = cabinets
            val base = cabinets[0]
            base.id = 255
            base.x = 0
            base.y = 0
            base.dir = Direction.RIGHT
            base.isBase = true
            base.visited = true

            // Set cabinet nodes
            graph.forEachIndexed { index, ints ->
                val cabinet = cabinets[index]
                val adjacent = ArrayList<Cabinet?>()

                // For all elements in the index of the IntArray
                ints.forEach {
                    // Get the adjacent Cabinet
                    val cab = getCabinetById(it)
                    // Add it to a Arraylist
                    if (cab != null) {
                        adjacent.add(cab)
                    } else {
                        adjacent.add(null)
                    }
                    /**
                     * This array is added to the Cabinet, this way a Cabinet has a list
                     * of adjacent Cabinets.
                     */
                    cabinet.adjacent = adjacent
                }
            }
            calculatePositions()
            emit(Resource.Success(cabinets))
        }.flowOn(Dispatchers.Default)
    }

    private fun calculatePositions() {

        cabinets.forEachIndexed { indexCabinets, _ ->
            val cabinet = cabinets[indexCabinets]
            val adjacent: ArrayList<Cabinet?>? = cabinet.adjacent

            adjacent?.forEachIndexed { indexAdjacent, _ ->
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

//    @Composable
//    fun Draw() {
//        for (cabinet in cabinets) {
//            cabinet.Draw(cabinet.x, cabinet.y, cabinet)
//        }
//    }

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
