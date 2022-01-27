package com.androidians.listsongs.service

import com.androidians.listsongs.data.Audio

object MusicLibrary {

    val audioList = ArrayList<Audio>()
    fun loadAudioList(list: List<Audio>) {
        for (item in list) {
            audioList.add(item)
        }
    }

}