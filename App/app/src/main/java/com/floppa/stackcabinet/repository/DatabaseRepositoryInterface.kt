package com.floppa.stackcabinet.repository

import com.floppa.stackcabinet.database.Resource
import com.floppa.stackcabinet.models.Component
import kotlinx.coroutines.flow.Flow

interface DatabaseRepositoryInterface {
    suspend fun getListComponents(): Flow<Resource<List<Component>>>

    suspend fun addComponents(component: Component)

    suspend fun removeComponent(component: Component)

    suspend fun updateComponent(component: Component)
}
