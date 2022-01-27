package com.androidians.listsongs.ui.adapters

import com.androidians.listsongs.data.Audio

interface SongListItemClickListener {
    fun onItemClick(audio: Audio, position: Int)
    fun onTitleClick(audio: Audio, position: Int)
}