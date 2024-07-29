package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song

class SongViewModel : ViewModel() {

    private val _items = MutableLiveData<List<Song>>()
    val items: LiveData<List<Song>> get() = _items

    init
    {
        _items.value = listOf(
            Song(1, "Item 1", 2.40, "empty"),
            Song(1, "Item 1", 2.40, "empty"),
            Song(1, "Item 1", 2.40, "empty")
        )
    }
}