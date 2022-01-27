package com.androidians.listsongs.data.room

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {

    private var INSTANCE: AppDatabase? = null
    private const val DATABASE_NAME = "songs"

    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

}