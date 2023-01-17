package com.floppa.stackcabinet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.floppa.stackcabinet.models.Component

@Database(
    entities = [Component::class],
    version = 1,
    exportSchema = false)
abstract class ComponentDatabase : RoomDatabase() {

    abstract fun componentDao(): ComponentDao

}