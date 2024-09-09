package com.example.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.SongsFinder
import com.example.musicplayer.repository.FavouritesRepository


class SongViewModel() : ViewModel() {

    private var _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    private var _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    private val _repeat = MutableLiveData<Boolean>()
    val repeat: LiveData<Boolean> get() = _repeat

    lateinit var favouritesRepository: FavouritesRepository

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

        newSongs.forEach {
            items.value?.add(0, it)
        }

    }

    fun initializeRepo(context: Context) {
        favouritesRepository = FavouritesRepository(context)
    }

    fun deleteSong(itemIndex: Int) {
        val newSongs = _items.value

        val r = newSongs?.removeAt(itemIndex)
        if (newSongs != null) {
            updateSongs(newSongs)
        }

        if (currentSong.value?.id == r?.id) {
            val itemsCount = getSongsCount()
            if (itemsCount > 1) {
                val newSong = if (itemIndex == itemsCount - 1) {
                    _items.value?.get(0)!!
                } else {
                    _items.value?.get(itemIndex)!!
                }
                newSong.isPlaying = AudioState.PLAY
                updateCurrentSong(newSong)
            } else {
                TODO("what if there is 1 song")
            }
        }

    }

    fun toggleRepetition() {
        _repeat.value = !_repeat.value!!
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
    }

}