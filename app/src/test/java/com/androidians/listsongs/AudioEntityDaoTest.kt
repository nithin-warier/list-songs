package com.androidians.listsongs

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.androidians.listsongs.data.room.AppDatabase
import com.androidians.listsongs.data.room.dao.AudioEntityDao
import com.androidians.listsongs.data.room.entities.AudioEntity
import junit.framework.TestCase.assertEquals

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AudioEntityDaoTest {

    private var audioEntityDao: AudioEntityDao? = null
    private var db: AppDatabase? = null

    @Before
    fun onCreateDB() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        audioEntityDao = db?.audioEntityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        db!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAudioEntity() {
        val entity = AudioEntity(1, "audio1", false)
        audioEntityDao?.insert(entity)
        val list = audioEntityDao?.getAll()
        assertEquals(list?.get(0)?.id, 1)
    }

    @Test
    @Throws(Exception::class)
    fun getAllAudioEntity() {
        val entity1 = AudioEntity(1, "audio1", false)
        val entity2 = AudioEntity(2, "audio2", false)
        audioEntityDao?.insert(entity1)
        audioEntityDao?.insert(entity2)
        val list = audioEntityDao?.getAll()
        assertEquals(list?.get(0)?.id, 1)
        assertEquals(list?.get(1)?.id, 2)
    }

}