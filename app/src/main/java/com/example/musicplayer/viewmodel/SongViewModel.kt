package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SongViewModel : ViewModel() {

    private val _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    init
    {
        getSongs()
    }

    private fun getSongs() = runBlocking {
        val SF = SongsFinder()
        var songList: ArrayList<Song>
        launch {
            songList = SF.getSongsFromDownload()
            _items.value = songList
        }
    }
}