package com.floppa.stackcabinet.repository

import com.floppa.stackcabinet.database.ComponentDao
import com.floppa.stackcabinet.database.Resource
import com.floppa.stackcabinet.models.Component
import com.floppa.stackcabinet.models.ProblemState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class DatabaseRepository
@Inject constructor(
    private val componentsDao: ComponentDao,
        ) : DatabaseRepositoryInterface {

    override suspend fun getListComponents(): Flow<Resource<List<Component>>> {
        return flow{
            emit(Resource.Loading())
            val dbResult = componentsDao.getListComponents()
            if (dbResult.isNotEmpty()){
                emit(Resource.Success(dbResult))
            } else {
                emit(Resource.Error(ProblemState.EMPTY))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun addComponents(component: Component) {
        componentsDao.addComponent(component)
    }

    override suspend fun removeComponent(component: Component){
        componentsDao.removeComponent(component)
    }

    override suspend fun updateComponent(component: Component){
        componentsDao.updateComponent(component)
    }

}