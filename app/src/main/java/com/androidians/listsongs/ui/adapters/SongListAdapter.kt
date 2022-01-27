package com.androidians.listsongs.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.androidians.listsongs.data.Audio
import com.androidians.listsongs.databinding.LayoutListItemSongsBinding
import com.androidians.listsongs.ui.adapters.viewholders.SongViewHolder

class SongListAdapter : ListAdapter<Audio, SongViewHolder>(Audio.DiffCallback) {

    private lateinit var listener: SongListItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding = LayoutListItemSongsBinding.inflate(layoutInflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val audio = getItem(position)
        holder.itemView.tag = audio
        holder.setData(audio, listener)
    }

    fun setOnItemClickListener(listener: SongListItemClickListener) {
        this.listener = listener
    }

}