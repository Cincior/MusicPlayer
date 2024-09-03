package com.example.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder


class SongViewModel() : ViewModel() {

    private var _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    private var _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    private val _repeat = MutableLiveData<Boolean>()
    val repeat: LiveData<Boolean> get() = _repeat

    init {
        _repeat.value = false
    }


    private fun updateSongs(newSongs: ArrayList<Song>) {
        _items.value = newSongs
    }
    fun updateCurrentSong(newSong: Song) {
        _currentSong.value = _items.value?.find {
            it.id == newSong.id
        }
    }

    /**
     * Method assigns all founded songs to _items while launching app
     */
    fun getSongs(context: Context) {
        val sf = SongsFinder(context)
        val songList = sf.getSongsFromDownload()
        _items.value = songList
    }
    fun getSongsUpdate(context: Context) {
        val sf = SongsFinder(context)
        val songList = sf.getSongsFromDownload()

        val newSongs = songList.filter { song ->
            song.id !in items.value!!.map { it.id }
        }

        println("nowe znalezione: " + newSongs)
        newSongs.forEach {
            items.value?.add(0, it)
        }

//        items.value?.find {
//            it.id == previousSong?.id
//        }.let {
//            it?.isPlaying = previousSong?.isPlaying ?: AudioState.NONE
//        }

        //forceUpdate()

        //fix after update
//        val prevSong = items.value?.find {
//            currentSong.value?.id == it.id
//        }
//        if (prevSong != null) {
//            // Trigger observers
//            _currentSong.value = items.value?.find {
//                currentSong.value!!.id == it.id
//            }
//        }

    }
//    fun getSongsUpdate(context: Context) {
//        val sf = SongsFinder(context)
//        val songList = sf.getSongsFromDownload()
//
//        //save previous state before insertion
//        val previousSong = currentSong.value?.copy()
//        println("powoduje trigger prev: " + previousSong)
//
//        _items.value = songList
//
//        if(previousSong != null) {
//            _currentSong.value = items.value?.find {
//                it.id == previousSong.id
//            }
//            currentSong.value?.isPlaying = previousSong.isPlaying
//        }
//        println("powoduje trigger past: " + currentSong.value)
//
//
//        forceUpdate()
//    }

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

    fun updateAllSongs() {
        val currentSongs = _items.value

        currentSongs?.forEach {
            it.isPlaying = AudioState.NONE
        }

        val s = currentSongs?.find {
            it.id == currentSong.value?.id
        }
        s?.isPlaying = currentSong.value?.isPlaying!!

        if (currentSongs != null) {
            updateSongs(currentSongs)
        }
    }

    fun updateCurrentSongState(song: Song) {
        val currentSongs = _items.value

        val s = currentSongs?.find {
            it.id == song.id
        }
        val ss = s?.isPlaying

        currentSongs?.forEach {
            it.isPlaying = AudioState.NONE
        }

        when (ss) {
            AudioState.PLAY -> s.isPlaying = AudioState.PAUSE
            AudioState.PAUSE -> s.isPlaying = AudioState.RESUME
            AudioState.RESUME -> s.isPlaying = AudioState.PAUSE
            AudioState.NONE -> s.isPlaying = AudioState.PLAY
            else -> s?.isPlaying = AudioState.PLAY
        }

    }

    fun getSongWithChangedPlayingState(): Song? {
        return items.value?.find {
            it.isPlaying != AudioState.NONE
        }
    }

    fun setRepetition(r: Boolean) {
        _repeat.value = r
    }

    fun toggleRepetition() {
        _repeat.value = !_repeat.value!!
    }

    fun changeCurrentSongState() {

        if (currentSong.value == null) {
            return
        }

        when (currentSong.value?.isPlaying) {
            AudioState.PLAY -> currentSong.value?.isPlaying = AudioState.PAUSE
            AudioState.PAUSE -> currentSong.value?.isPlaying = AudioState.RESUME
            AudioState.RESUME -> currentSong.value?.isPlaying = AudioState.PAUSE
//            AudioState.NONE -> currentSong.value?.isPlaying = AudioState.PLAY
            AudioState.END -> currentSong.value?.isPlaying = AudioState.PLAY
            else -> println("ERROR CHANGING STATE")
        }
        updateCurrentSong(currentSong.value!!)

        println("tu " + currentSong.value)
        println("tu " + items.value)

    }

    fun getSongsCount() = items.value?.size ?: 0

    fun setCurrentSongNext() {
        val currentIndexInItems = items.value?.indexOf(currentSong.value)
        if (currentIndexInItems != null) {
            currentSong.value?.isPlaying = AudioState.NONE
            if (currentIndexInItems == getSongsCount() - 1) {
                _currentSong.value = items.value?.get(0).also {
                    it?.isPlaying = AudioState.PLAY
                }
            } else {
                _currentSong.value = items.value?.get(currentIndexInItems + 1).also {
                    it?.isPlaying = AudioState.PLAY
                }
            }
        }
    }

    fun setCurrentSongPrev() {
        val currentIndexInItems = items.value?.indexOf(currentSong.value)
        if (currentIndexInItems != null) {
            currentSong.value?.isPlaying = AudioState.NONE
            if (currentIndexInItems == 0) {
                _currentSong.value = items.value?.get(getSongsCount() - 1).also {
                    it?.isPlaying = AudioState.PLAY
                }
            } else {
                _currentSong.value = items.value?.get(currentIndexInItems - 1).also {
                    it?.isPlaying = AudioState.PLAY
                }
            }
        }
        println("czemu dziala: " + currentSong.value)
    }

}