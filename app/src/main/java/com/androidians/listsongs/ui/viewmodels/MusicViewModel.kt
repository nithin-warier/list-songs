package com.androidians.listsongs.ui.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidians.listsongs.network.Event
import com.androidians.listsongs.network.Resource
import com.androidians.listsongs.service.MusicService
import com.androidians.listsongs.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong: LiveData<MediaMetadataCompat?> = _curPlayingSong

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(getApplication<Application>())
    lateinit var mediaController: MediaControllerCompat
    private val mediaBrowser = MediaBrowserCompat(
        getApplication<Application>(),
        ComponentName(
            getApplication<Application>(),
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    )
    val transportControls: MediaControllerCompat.TransportControls get() = mediaController.transportControls

    fun mediaBrowserConnect() {
        mediaBrowser.connect()
    }

    /**
     * Callbacks for browser service related events like isConnected, isConnectionFailed, suspended etc..
     */
    private inner class MediaBrowserConnectionCallback(private val context: Context) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Log.d("MusicServiceConnection", "CONNECTED")
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            Log.d("MusicServiceConnection", "SUSPENDED")

            _isConnected.postValue(Event(Resource.error(
                "The connection was suspended", false
            )))
        }

        override fun onConnectionFailed() {
            Log.d("MusicServiceConnection", "FAILED")

            _isConnected.postValue(Event(Resource.error(
                "Couldn't connect to media browser", false
            )))
        }
    }

    /**
     * To control the state of media playback, means whether playing, paused, stopped,
     * fast forwarding, rewinding, buffering, connecting, skipToNext, skipToPrevious etc.
     */
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event) {
                Constants.NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldn't connect to the server. Please check your internet connection.",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }








    //private val musicServiceConnection = MusicServiceConnection(getApplication<Application>())
    // media browser variables
    /*val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState
    private val _mediaItems = MutableLiveData<Resource<List<Audio>>>()
    val mediaItems: LiveData<Resource<List<Audio>>> = _mediaItems

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Audio(
                        it.description.mediaId!!.toLong(),
                        it.description.title.toString(),
                        Date(),
                        it.description.mediaUri!!,
                        false
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Audio, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.id.toString() ==
            curPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.id.toString(), null)
        }
    }*/

    override fun onCleared() {
        super.onCleared()
        //musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}