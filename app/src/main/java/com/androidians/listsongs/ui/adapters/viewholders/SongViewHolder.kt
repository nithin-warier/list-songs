package com.androidians.listsongs.ui.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.androidians.listsongs.R
import com.androidians.listsongs.data.Audio
import com.androidians.listsongs.databinding.LayoutListItemSongsBinding
import com.androidians.listsongs.ui.adapters.SongListItemClickListener

class SongViewHolder(private val binding: LayoutListItemSongsBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setData(audio: Audio, listener: SongListItemClickListener) {
        binding.titleTV.text = audio.title
        setDrawable(audio)
        binding.favoriteIV.setOnClickListener{
            listener.onItemClick(audio, bindingAdapterPosition)
        }
        binding.titleTV.setOnClickListener{
            listener.onTitleClick(audio, bindingAdapterPosition)
        }
    }

    private fun setDrawable(audio: Audio) {
        if (audio.isFavorite) {
            binding.favoriteIV.setImageResource(R.drawable.ic_favorite_selected)
        } else {
            binding.favoriteIV.setImageResource(R.drawable.ic_favorite)
        }
    }
}