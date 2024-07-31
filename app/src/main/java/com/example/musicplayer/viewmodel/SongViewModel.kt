package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder

class SongViewModel : ViewModel() {

    private val _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    init
    {
        val SF = SongsFinder()
        val songList = SF.getSongsFromDwonload()
        _items.value = songList
    }
}