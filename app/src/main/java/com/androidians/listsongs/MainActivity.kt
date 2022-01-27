package com.androidians.listsongs

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidians.listsongs.data.Audio
import com.androidians.listsongs.databinding.ActivityMainBinding
import com.androidians.listsongs.ui.adapters.SongListAdapter
import com.androidians.listsongs.ui.adapters.SongListItemClickListener
import com.androidians.listsongs.ui.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PERMISSION_STORAGE_PERMISSION = 1
    }

    private val viewModel: MainActivityViewModel by viewModels()
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkStoragePermission()
    }

    private fun showAudioList() {
        viewModel.loadFavorites()
        setView()
    }

    private fun setView() {
        val songListAdapter = SongListAdapter()
        songListAdapter.setOnItemClickListener(object : SongListItemClickListener {
            override fun onItemClick(audio: Audio, position: Int) {
                audio.isFavorite = !audio.isFavorite
                songListAdapter.notifyItemChanged(position)
                // add to db
                viewModel.addToFavorites(audio)
            }

            override fun onTitleClick(audio: Audio, position: Int) {
                MediaPlayer().apply {
                    setDataSource(applicationContext, audio.contentUri) //to set media source and send the object to the initialized state
                    prepare() //to send the object to prepared state
                    start() //to start the music and send the object to the started state
                }
            }
        })
        binding.songListRV.also { view ->
            view.layoutManager = LinearLayoutManager(this)
            view.adapter = songListAdapter
        }

        setObservers(songListAdapter)
    }

    private fun setObservers(songListAdapter: SongListAdapter) {
        viewModel.audioLiveDate.observe(this, { audios ->
            songListAdapter.submitList(audios)
        })
        viewModel.favoritesLiveDate.observe(this, { favorites ->
            viewModel.loadAudios(favorites)
        })
    }

    private fun checkStoragePermission() {
        val storagePermission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (isPermissionGranted(storagePermission)) {
            showAudioList()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(storagePermission, REQUEST_PERMISSION_STORAGE_PERMISSION)
            }
        }

    }

    private fun isPermissionGranted(storagePermission: Array<String>): Boolean {
        var flag = false
        for (permission in storagePermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    flag = true
                } else {
                    return false
                }
            }
        }
        return flag
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showAudioList()
        }
    }
}