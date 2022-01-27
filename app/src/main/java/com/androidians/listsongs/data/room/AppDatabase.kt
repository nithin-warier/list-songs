package com.androidians.listsongs.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidians.listsongs.data.room.dao.AudioEntityDao
import com.androidians.listsongs.data.room.entities.AudioEntity

@Database(entities = [AudioEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioEntityDao(): AudioEntityDao

}