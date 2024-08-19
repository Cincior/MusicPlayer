package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder

class SongViewModel(private val application: Application) : AndroidViewModel(application) {

    private lateinit var allItemsWithPreviousState: ArrayList<Song>
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
        val sfO = SongsFinder(application)
        val songList = sf.getSongsFromDownload()
        val songListOriginal = sfO.getSongsFromDownload()
        _items.value = songList
        allItemsWithPreviousState = songListOriginal
    }

    private fun updateSongs(newSongs: ArrayList<Song>) {
        _items.value = newSongs
    }

    private fun updateAllSongs(changedSong: Song) {
        allItemsWithPreviousState.forEach {
            it.isPlaying = AudioState.NONE
        }
        allItemsWithPreviousState.find {
            it.id == changedSong.id
        }.let {
            it?.isPlaying = changedSong.isPlaying
        }
    }

    fun setDefaultSongs()
    {
        updateSongs(allItemsWithPreviousState)
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
            AudioState.END -> foundedSong?.isPlaying = AudioState.NONE
            else -> foundedSong?.isPlaying = AudioState.PLAY
        }
        if (currentSongs != null) {
            updateSongs(currentSongs)
            if (foundedSong != null) {
                updateAllSongs(foundedSong.copy())
            }
        }
    }



    fun filterSongs(query: String)
    {
        val allSongs = ArrayList(allItemsWithPreviousState)
        val filteredSongs = getFilteredSongs(query, allSongs)
        updateSongs(filteredSongs)
//        val currentSongs = _items.value
//        var foundedSongState: AudioState? = null
//        val foundedSongId: Long?
//        val foundedSong = currentSongs?.find {
//            it.isPlaying == AudioState.PLAY || it.isPlaying == AudioState.PAUSE
//        }
//        foundedSongState = foundedSong?.isPlaying
//        foundedSongId = foundedSong?.id
//        val filteredSongs = getFilteredSongs(query)
//        filteredSongs.forEach {
//            it.isPlaying = AudioState.NONE
//        }
//        if(foundedSong == null) {
//            updateSongs(filteredSongs)
//            return
//        }
//
//        val songToChange = filteredSongs.find {
//            it.id == foundedSongId
//        }
//        songToChange?.isPlaying = foundedSongState!!
//        println("filtred z filterSong" +filteredSongs)
//        println("wczesniejsze" +foundedSong)
//        updateSongs(filteredSongs)

    }

    private fun getFilteredSongs(query: String, allSongs: ArrayList<Song>): ArrayList<Song> {
        val filteredSongs = allSongs.filter {
            it.title.startsWith(query, true)
        }.let { ArrayList(it) }
        println("PO FILTRZE" + filteredSongs)

        return filteredSongs
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
