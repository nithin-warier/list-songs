package com.androidians.listsongs.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio")
data class AudioEntity(
    @PrimaryKey @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "uri") var uri: String,
    @ColumnInfo(name = "favorite") var favorite: Boolean
)