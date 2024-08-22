package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder

class SongViewModel(private val application: Application) : AndroidViewModel(application) {

    private var _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items


    private fun updateSongs(newSongs: ArrayList<Song>) {
        _items.value = newSongs
    }

    /**
     * Method assigns all founded songs to _items
     */
    fun getSongs() {
        val sf = SongsFinder(application)
        val songList = sf.getSongsFromDownload()
        _items.value = songList
    }
    fun getSongsUpdate() {
        val sf = SongsFinder(application)
        val songList = sf.getSongsFromDownload()

        //save previous state before insertion
        val previousSong = getSongWithChangedPlayingState()

        _items.value = songList

        items.value?.find {
            it.id == previousSong?.id
        }.let {
            it?.isPlaying = previousSong?.isPlaying ?: AudioState.NONE
            println("znalazlem: " + it)
        }

        forceUpdate()
    }

    fun forceUpdate() {
        val songs = items.value
        updateSongs(songs!!)
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
            AudioState.PAUSE -> foundedSong?.isPlaying = AudioState.PLAY
            //AudioState.END -> foundedSong?.isPlaying = AudioState.END
            else -> foundedSong?.isPlaying = AudioState.PLAY
        }
        if (currentSongs != null) {
            updateSongs(currentSongs)
        }
    }

    fun getSongWithChangedPlayingState(): Song? {
        return items.value?.find {
            it.isPlaying != AudioState.NONE
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
