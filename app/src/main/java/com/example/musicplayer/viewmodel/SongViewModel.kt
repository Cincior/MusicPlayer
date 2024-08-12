package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SongViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    init {
        //getSongs()
    }

    /**
     * Method assigns all founded songs to _items
     */
    fun getSongs() {
        val sf = SongsFinder(application)
        val songList = sf.getSongsFromDownload()
        _items.value = songList
    }

    private fun updateSongs(newSongs: ArrayList<Song>) {
        _items.value = newSongs
    }

    fun deleteSong(id: Long) {
        val newSongs = _items.value
        val r = newSongs?.removeIf { it.id == id }
        if (newSongs != null) {
            updateSongs(newSongs)
        }
    }

    fun updatePlayingState(song: Song) {
        val currentSongs = _items.value
        var foundedSongState: AudioState? = null
        val foundedSong = currentSongs?.find {
            it.id == song.id
        }
        foundedSongState = foundedSong?.isPlaying
        currentSongs?.forEach {
            it.isPlaying = AudioState.NONE
        }
        when (foundedSongState) {
            AudioState.PLAY -> foundedSong?.isPlaying = AudioState.PAUSE
            else -> foundedSong?.isPlaying = AudioState.PLAY
        }
        if (currentSongs != null) {
            updateSongs(currentSongs)
        }
    }
}


//class SongViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return SongViewModel(application) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
