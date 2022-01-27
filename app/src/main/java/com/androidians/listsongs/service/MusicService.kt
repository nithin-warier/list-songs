package com.androidians.listsongs.service

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import com.androidians.listsongs.service.callbacks.MusicPlaybackPreparer
import com.androidians.listsongs.service.callbacks.MusicPlayerEventListener
import com.androidians.listsongs.service.callbacks.MusicPlayerNotificationListener
import com.androidians.listsongs.utils.Constants.MEDIA_ROOT_ID
import com.androidians.listsongs.utils.Constants.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var musicSource: MusicSource
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var curPlayingSong: MediaMetadataCompat? = null
    var isForegroundService = false
    private var isPlayerInitialized = false

    companion object {
        var curSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        musicSource = MusicSource()
        serviceScope.launch {
            musicSource.fetchMediaData()
        }

        createExoPlayer()
        createMediaSession()
        createMusicNotificationManager()

        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) {
            curPlayingSong = it
            preparePlayer(
                musicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        when(parentMediaId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if(isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if(!isPlayerInitialized && musicSource.songs.isNotEmpty()) {
                            preparePlayer(musicSource.songs, musicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if(!resultsSent) {
                    result.detach()
                }
            }
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this,"ListSongs App"))

        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    private fun createMusicNotificationManager() {
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDuration = exoPlayer.duration
        }
    }

    private fun createMediaSession() {
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken
    }

    private fun createExoPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        exoPlayer = SimpleExoPlayer.Builder(this).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return musicSource.songs[windowIndex].description
        }
    }

}
