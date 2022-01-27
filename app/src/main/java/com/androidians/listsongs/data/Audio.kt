package com.androidians.listsongs.data

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class Audio(val id: Long, val title: String, val date: Date, val contentUri: Uri, var isFavorite: Boolean) {

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Audio>() {
            override fun areItemsTheSame(oldItem: Audio, newItem: Audio) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Audio, newItem: Audio) = oldItem == newItem
        }
    }
}