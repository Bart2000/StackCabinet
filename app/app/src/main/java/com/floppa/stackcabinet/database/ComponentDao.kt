package com.floppa.stackcabinet.database

import androidx.room.*
import com.floppa.stackcabinet.models.Component

@Dao
interface ComponentDao {

    @Query("SELECT * FROM componentTable")
    suspend fun getListComponents(): List<Component>

    @Insert
    suspend fun addComponent(component: Component)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateComponent(component: Component)

    @Delete
    suspend fun removeComponent(component: Component)
}