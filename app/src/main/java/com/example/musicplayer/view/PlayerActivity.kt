package com.example.musicplayer.view

import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.viewmodel.SongViewModel
import com.example.musicplayer.viewmodel.ViewModelSingleton
import java.io.IOException

class PlayerActivity : AppCompatActivity() {
    private val songViewModel: SongViewModel by lazy {
        ViewModelSingleton.getSharedViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val currentSong = songViewModel.getSongWithChangedPlayingState()
        try {
            val thumbnail = currentSong?.let {
                contentResolver.loadThumbnail(
                    it.uri,
                    Size(300,300),
                    null
                )
            }
            findViewById<ImageView>(R.id.albumThumbnail).setImageBitmap(thumbnail)
        } catch (e: IOException) {
            findViewById<ImageView>(R.id.albumThumbnail).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_name))
        }
        findViewById<Button>(R.id.startS).setOnClickListener{
            Intent(applicationContext, MusicPlayerService::class.java).also {
                it.putExtra("title", currentSong?.title)
                it.action = MusicPlayerService.Actions.Start.toString()
                startService(it)
            }
        }

        findViewById<Button>(R.id.stopS).setOnClickListener{
            Intent(applicationContext, MusicPlayerService::class.java).also {
                it.action = MusicPlayerService.Actions.Stop.toString()
                startService(it)
            }
        }

        val buttonRepeat = findViewById<ImageButton>(R.id.btnRepeat)
        initializeButtonRepeat(buttonRepeat)

    }

    private fun initializeButtonRepeat(buttonRepeat: ImageButton) {
        buttonRepeat.setOnClickListener{
            if (songViewModel.repeat.value == true) {
                songViewModel.setRepetition(false)
                buttonRepeat.setColorFilter(getColor(R.color.skyBlue))
            } else {
                songViewModel.setRepetition(true)
                buttonRepeat.setColorFilter(getColor(R.color.darkBlue))
            }
            findViewById<TextView>(R.id.head).text = songViewModel.repeat.value.toString()
        }
    }
}