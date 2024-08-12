package com.example.musicplayer.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.media.AudioPlayer
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.mainActivityPackage.*
import com.example.musicplayer.viewmodel.SongViewModel

class MainActivity : AppCompatActivity() {
    companion object {
        var permissionGranted = false
        var deletionId = -1 // Id of particular Song that can be deleted
        var listId = -1 // Id of item in recyclerView that can be deleted
    }

    private var audioPlayer: AudioPlayer? = null
    private var previousPlayingPosition = -1 // stores position of song that is currently playing
    private var previousHolder: ItemViewHolder? = null //stores previously clicked item ViewHolder

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> // To ask user about deletion of song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(R.layout.activity_main)

        // GET PERMISSIONS FIRST
        getPermission(this)
        if (!permissionGranted) {
            finish()
        }

        // Registering deletion
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
            {
                if (it.resultCode == RESULT_OK) {
                    if (deletionId != -1) {
                        songViewModel.deleteSong(deletionId.toLong())
                        songAdapter.notifyItemRangeChanged(listId, songViewModel.items.value!!.size)
                        songAdapter.notifyItemRemoved(listId)
                        deletionId = -1
                        listId = -1
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Song couldn't be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        val recyclerView = findViewById<RecyclerView>(R.id.songList)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        songViewModel.getSongs()
        songAdapter = SongAdapter(songViewModel.items.value!!)
        recyclerView.adapter = songAdapter

        songViewModel.items.observe(this) { songs ->
            songAdapter.updateSongs(songs)
        }

        // Passing to adapter implemented functions of interface
        songAdapter.setOnClickListener(object : SongAdapter.IonClickListener {
            /**
             * Function lets user open menu from which the user can delete an audio file.
             * @param position position of clicked song
             * @param song particular Song object that has been clicked
             */
            override fun onLongClick(position: Int, song: Song) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder
                    .setTitle(song.title)
                    .setItems(arrayOf("Delete", "chuj")) { dialog, which ->
                        if (which == 0) {
                            val deleteRequest =
                                MediaStore.createDeleteRequest(contentResolver, listOf(song.uri))
                            intentSenderLauncher.launch(
                                IntentSenderRequest.Builder(deleteRequest).build()
                            )
                            deletionId = song.id.toInt()
                            listId = position
                        } else {
                            Toast.makeText(this@MainActivity, "clicked 2", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            /**
             * Function responsible for playing or stopping music, showing animation and changing currentlyPlayingPosition variable
             * @param holder contains whole song UI elements
             * @param song clicked Song object
             */
            override fun onClick(holder: ItemViewHolder, song: Song) {
                val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.song_clicked_animation)
                holder.itemView.startAnimation(animation)

//                if (previousPlayingPosition == holder.bindingAdapterPosition) {
//                    if (audioPlayer!!.isPlaying()) {
//                        audioPlayer?.pauseSong()
//                        songViewModel.updatePlayingState(song)
//                        songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
//                    } else {
//                        audioPlayer?.resumeSong()
//                        songViewModel.updatePlayingState(song)
//                        songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
//                    }
//                } else {
//                    audioPlayer?.stopSong()
//                    audioPlayer = AudioPlayer(this@MainActivity, song.uri).apply {
//                        playSong()
//                    }
//                    songViewModel.updatePlayingState(song)
//                    songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
//                    songAdapter.notifyItemChanged(previousPlayingPosition)
//                    previousPlayingPosition = holder.bindingAdapterPosition
//                    previousHolder = holder
//
//                }
                //WYSZUKAM ITEMEK W SEARCHVIEW DAM ZEBY GRAL I POTEM PAUZE DAM JUZ Z WIDOKU NORMLANEGO BEZ WYSZUKANJE FRAZY TO BUGUJE SIE
                if (song.isPlaying == AudioState.PLAY) {
                    audioPlayer?.pauseSong()
                    songViewModel.updatePlayingState(song)
                    songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
                } else if(song.isPlaying == AudioState.PAUSE) {
                    audioPlayer?.resumeSong()
                    songViewModel.updatePlayingState(song)
                    songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
                } else {
                    audioPlayer?.stopSong()
                    audioPlayer = AudioPlayer(this@MainActivity, song.uri).apply {
                        playSong()
                    }
                    songViewModel.updatePlayingState(song)
//                    songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
//                    if(previousPlayingPosition != -1) {
//                        songAdapter.notifyItemChanged(previousPlayingPosition)
//                    }
                    songAdapter.notifyDataSetChanged()
                    previousPlayingPosition = holder.bindingAdapterPosition
                    previousHolder = holder

                }
            }
        })

        val searchView = findViewById<SearchView>(R.id.searchSong)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (query != "") {
                    songViewModel.filterSongs(query)
                    println(songViewModel.items.value!!.size)
                    songAdapter.notifyDataSetChanged()
                } else {
                    songViewModel.setDefaultSongs()
                    songAdapter.notifyDataSetChanged()
                    println("tutaj sa wszystkie z play " + songViewModel.items.value!!.filter {
                        it.isPlaying == AudioState.PLAY
                    })
                }
                return true
            }

        })


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeReadMemory) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                permissionGranted = true
            } else {
                Toast.makeText(this, "Odmowa uprawnien", Toast.LENGTH_SHORT).show()
                permissionGranted = false
            }
        }
    }

}