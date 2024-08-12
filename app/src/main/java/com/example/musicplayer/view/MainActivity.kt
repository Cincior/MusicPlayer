package com.example.musicplayer.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.media.AudioPlayer
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

        // registering deletion
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
             * @param item particular Song object that has been clicked
             */
            override fun onLongClick(position: Int, item: Song) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder
                    .setTitle(item.title)
                    .setItems(arrayOf("Delete", "chuj")) { dialog, which ->
                        if (which == 0) {
                            val deleteRequest =
                                MediaStore.createDeleteRequest(contentResolver, listOf(item.uri))
                            intentSenderLauncher.launch(
                                IntentSenderRequest.Builder(deleteRequest).build()
                            )
                            deletionId = item.id.toInt()
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
             * @param item clicked Song object
             */
            override fun onClick(holder: ItemViewHolder, item: Song) {
                val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.song_clicked_animation)
                holder.itemView.startAnimation(animation)

                if (previousPlayingPosition == holder.bindingAdapterPosition) {
                    //previousHolder = null
                    if (audioPlayer!!.isPlaying()) {
                        audioPlayer?.pauseSong()
                        pauseAppearance(holder, songAdapter, this@MainActivity)
                    } else {
                        audioPlayer?.resumeSong()
                        playAppearance(holder, songAdapter, this@MainActivity)
                    }
                    //previousPlayingPosition = -1
                    //previousHolder = null
                } else {
                    audioPlayer?.stopSong()
                    audioPlayer = AudioPlayer(this@MainActivity, item.uri).apply {
                        playSong()
                    }
                    previousPlayingPosition = holder.bindingAdapterPosition
                    if (previousHolder != null) {
                        changeAppearance(previousHolder!!, holder, songAdapter, this@MainActivity)
                    } else {
                        playAppearance(holder, songAdapter, this@MainActivity)
                    }
                    previousHolder = holder

                }
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