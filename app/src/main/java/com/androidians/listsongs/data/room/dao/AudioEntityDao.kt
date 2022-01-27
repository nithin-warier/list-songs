package com.androidians.listsongs.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidians.listsongs.data.room.entities.AudioEntity

@Dao
interface AudioEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(audioEntity: AudioEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(audioEntityList: List<AudioEntity>)

    @Query("SELECT * FROM audio")
    fun getAll(): List<AudioEntity>

}