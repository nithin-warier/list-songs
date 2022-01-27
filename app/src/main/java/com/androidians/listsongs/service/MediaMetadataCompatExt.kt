package com.androidians.listsongs.service

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.androidians.listsongs.data.Audio
import java.util.*

fun MediaMetadataCompat.toAudio(): Audio? {
    return description?.let {
        Audio(
            it.mediaId?.toLong()!!,
            it.title.toString(),
            Date(),
            Uri.parse(it.mediaUri.toString()),
            isFavorite = false
        )
    }
}