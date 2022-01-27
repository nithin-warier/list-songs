package com.androidians.listsongs.ui.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androidians.listsongs.data.Audio
import com.androidians.listsongs.data.room.DatabaseBuilder
import com.androidians.listsongs.data.room.entities.AudioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainActivityViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val audios = MutableLiveData<List<Audio>>()
    val audioLiveDate: LiveData<List<Audio>> get() = audios

    private val favorites = MutableLiveData<List<AudioEntity>>()
    val favoritesLiveDate: LiveData<List<AudioEntity>> get() = favorites
    private var contentObserver: ContentObserver? = null

    fun loadAudios(favorites: List<AudioEntity>) {
        viewModelScope.launch {
            val audioList = queryAudios(favorites)
            audios.postValue(audioList)

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadAudios(favorites)
                }
            }
        }
    }

    fun loadFavorites() : List<AudioEntity> {
        val audios = ArrayList<AudioEntity>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val db = DatabaseBuilder.getInstance(getApplication<Application>())
                val favoritesList = db.audioEntityDao().getAll()
                favorites.postValue(favoritesList)
                audios.addAll(favoritesList)
            }
        }
        return audios
    }

    fun addToFavorites(audio: Audio) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val entity = AudioEntity(audio.id, audio.title, audio.contentUri.toString(), audio.isFavorite)
                DatabaseBuilder.getInstance(getApplication<Application>()).audioEntityDao().insert(entity)
            }
        }
    }

    private suspend fun queryAudios(favorites: List<AudioEntity>) : List<Audio> {
        val audios = mutableListOf<Audio>()
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED
            )

            getApplication<Application>().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val displayName = cursor.getString(displayNameColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    var isFavorite = false
                    for (favItem in favorites) {
                        if (favItem.id == id) {
                            isFavorite = favItem.favorite
                            break
                        }
                    }
                    val audio = Audio(id, displayName, dateModified, contentUri, isFavorite)
                    audios += audio
                }
            }
        }

        return audios
    }
}

/**
 * Convenience extension method to register a content observer - given a lambda.
 */
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}