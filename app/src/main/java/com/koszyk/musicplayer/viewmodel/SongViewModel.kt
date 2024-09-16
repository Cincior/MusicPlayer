package com.koszyk.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.koszyk.musicplayer.media.MusicPlayerService
import com.koszyk.musicplayer.model.AudioState
import com.koszyk.musicplayer.model.Song
import com.koszyk.musicplayer.model.SongsFinder
import com.koszyk.musicplayer.repository.FavouritesRepository
import com.koszyk.musicplayer.view.fragment.SettingsFragment.Companion.DEVICE_ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SongViewModel() : ViewModel() {

    private var _items = MutableLiveData<ArrayList<Song>>()
    val items: LiveData<ArrayList<Song>> get() = _items

    private var _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    private val _repeat = MutableLiveData<Boolean>()
    val repeat: LiveData<Boolean> get() = _repeat

    private val _isSongsLoaded = MutableLiveData<Boolean>()
    val isSongsLoaded: LiveData<Boolean> get() = _isSongsLoaded

    val _isCheckedStateChanged = MutableLiveData<Boolean>()
    val isCheckedStateChanged: LiveData<Boolean> get() = _isCheckedStateChanged

    lateinit var favouritesRepository: FavouritesRepository

    init {
        _repeat.value = false
        _isSongsLoaded.value = false
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

    fun getSongsFromChosenFolders(context: Context) {
        _isSongsLoaded.value = false
        val db = Firebase.firestore
        val docRef = db.collection("folders").document(DEVICE_ID)
        var folderMap: Map<String, Any>
        val folderList: MutableList<String> = mutableListOf()
        docRef
            .get()
            .addOnSuccessListener { document ->
                folderMap = document.data ?: emptyMap()
                folderMap.forEach {
                    if (it.value == true) {
                        folderList.add("%" + it.key.substringAfterLast("/") + "%")
                    }
                }

                val sf = SongsFinder(context)
                val songList = sf.getSongsFromGivenDirectories(folderList.toTypedArray())
                _items.value = songList
                _isSongsLoaded.value = true
            }

    }

    suspend fun getSongsFromChosenFoldersSuspend(context: Context) {
        withContext(Dispatchers.Main) {
            _isSongsLoaded.value = false
        }

        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            val docRef = db.collection("folders").document(DEVICE_ID)

            val documentSnapshot = docRef.get().await()

            val folderMap = documentSnapshot.data ?: emptyMap()
            val folderList: MutableList<String> = mutableListOf()

            folderMap.forEach {
                if (it.value == true) {
                    println("tutaj dodaje %: " + it.key)
                    folderList.add(it.key + "/%")
                }
            }

            val sf = SongsFinder(context)
            val songList = sf.getSongsFromGivenDirectories(folderList.toTypedArray())

            // Save previous song state
            var stateBeforeUpdate = currentSong.value?.isPlaying
            if (stateBeforeUpdate == AudioState.PLAY) {
                stateBeforeUpdate = AudioState.RESUME
            }

            withContext(Dispatchers.Main) {
                _items.value = songList
                _currentSong.value = _items.value?.find {
                    it.id == currentSong.value?.id
                }.also {
                    it?.isPlaying = stateBeforeUpdate ?: AudioState.NONE
                }

                _isSongsLoaded.value = true
            }

        }
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
//      BUGGED
//    fun getSongsUpdateFromChosenFolders(context: Context) {
//        val sf = SongsFinder(context)
//        val songList = sf.getSongsFromGivenDirectories(chosenFolders)
//
//        val newSongs = songList.filter { song ->
//            song.id !in items.value!!.map { it.id }
//        }
//
//        newSongs.forEach {
//            items.value?.add(0, it)
//        }
//        println("po update: " + songList)
//
//    }

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

    fun changeCurrentSongStateAfterAudioFocusLost() {
        if (currentSong.value == null) {
            return
        }

        currentSong.value!!.isPlaying = AudioState.PAUSE

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

    fun changeIsCheckedState(state: Boolean) {
        _isCheckedStateChanged.value = state
    }

}