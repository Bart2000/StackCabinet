package com.floppa.stackcabinet.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "componentTable")
data class Component(
    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "type")
    val type: ComponentsTypes,

    @ColumnInfo(name = "cabinetId")
    val cabinetId: Int?,

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val index: Int?,
)