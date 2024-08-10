package com.example.musicplayer.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.media.AudioPlayer
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel



class MainActivity : AppCompatActivity()
{
    companion object
    {
        var permissionGranted = false
        var deletionId = -1 // Id of particular Song that can be deleted
        var listId = -1 // Id of item in recyclerView that can be deleted
    }
    private var audioPlayer: AudioPlayer? = null
    private var previousPlayingPosition = -1 // stores position of song that is currently playing
    private var previousHolder: ItemViewHolder? = null
    private val requestCodeReadMemory = 0
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> // To ask user about deletion of song

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(R.layout.activity_main)

        // GET PERMISSIONS FIRST
        getPermission()
        if(!permissionGranted)
        {
            finish()
        }


        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
        {
            if (it.resultCode == RESULT_OK) {
                if(deletionId != -1)
                {
                    songViewModel.deleteSong(deletionId.toLong())
                    songAdapter.notifyItemRangeChanged(listId,songViewModel.items.value!!.size)
                    songAdapter.notifyItemRemoved(listId)
                    deletionId = -1
                    listId = -1
                }
            } else {
                Toast.makeText(this@MainActivity, "Song couldn't be deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.songList)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        songViewModel.getSongs()
        songAdapter = SongAdapter(songViewModel.items.value!!, this)
        recyclerView.adapter = songAdapter

        songViewModel.items.observe(this, Observer { songs ->
            songAdapter.updateSongs(songs)
        })

        songAdapter.setOnClickListener(object : SongAdapter.IonClickListener{
            override fun onLongClick(position: Int, item: Song)
            {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder
                    .setTitle(item.title)
                        .setItems(arrayOf("Delete", "chuj")) { dialog, which ->
                        if(which == 0)
                        {
                            val deleteRequest = MediaStore.createDeleteRequest(contentResolver, listOf(item.uri))
                            intentSenderLauncher.launch(IntentSenderRequest.Builder(deleteRequest).build())
                            deletionId = item.id.toInt()
                            listId = position
                        }
                        else
                        {
                            Toast.makeText(this@MainActivity, "clicked 2", Toast.LENGTH_SHORT).show()
                        }
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            override fun onClick(holder: ItemViewHolder, item: Song) {
                val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.song_clicked_animation)
                holder.itemView.startAnimation(animation)

                if (previousPlayingPosition != -1)
                {
                    previousHolder?.let { changeAppearance(it) }
                    audioPlayer?.stopSong()
                }

                if(previousPlayingPosition == holder.bindingAdapterPosition)
                {
                    audioPlayer?.stopSong()
                    previousPlayingPosition = -1
                    previousHolder?.let { resetAppearance(it) }
                    previousHolder = null
                }
                else
                {
                    audioPlayer = AudioPlayer(this@MainActivity, item.uri).apply {
                        playSong()
                    }
                    previousPlayingPosition = holder.bindingAdapterPosition
                    if(previousHolder != null)
                    {
                        changeAppearance(previousHolder!!, holder)
                    }
                    else
                    {
                        changeAppearance(holder)
                    }
                    previousHolder = holder

                }
                //songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
            }

        })


    }

    private fun changeAppearance(previousHolder: ItemViewHolder, currentHolder: ItemViewHolder)
    {
        currentHolder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.skyBlue))
        currentHolder.playingImage.visibility = View.VISIBLE
        songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)

        previousHolder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        previousHolder.playingImage.visibility = View.INVISIBLE
        songAdapter.notifyItemChanged(previousHolder.bindingAdapterPosition)

    }
    private fun changeAppearance(currentHolder: ItemViewHolder)
    {
        currentHolder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.skyBlue))
        currentHolder.playingImage.visibility = View.VISIBLE
        songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)
    }

    private fun resetAppearance(currentHolder: ItemViewHolder)
    {
        currentHolder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        currentHolder.playingImage.visibility = View.INVISIBLE
        songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)
    }

    private fun checkClickedElement(
        currentlyPlayingPosition: Int,
        holder: ItemViewHolder,
        position: Int
    )
    {
        if(currentlyPlayingPosition == position)
        {
            holder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.skyBlue))
            holder.playingImage.visibility = View.VISIBLE
        }
        else
        {
            holder.titleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            holder.playingImage.visibility = View.INVISIBLE
        }
    }

    /**
     * Asks user for needed permission (to get audio files from memory) in way that depends on currently using system
     * assign permissionGranted = true if user gave permission
     */
    private fun getPermission()
    {
        if(getSystemVersion() >= 33)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    requestCodeReadMemory)
            }
            else
            {
                permissionGranted = true
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    requestCodeReadMemory)
            }
            else
            {
                permissionGranted = true
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeReadMemory) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                permissionGranted = true
            }
            else
            {
                Toast.makeText(this, "Odmowa uprawnien", Toast.LENGTH_SHORT).show()
                permissionGranted = false
            }
        }
    }

    /**
     * @return version of android system
     */
    private fun getSystemVersion(): Int
    {
        return android.os.Build.VERSION.SDK_INT
    }

}