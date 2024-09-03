package com.example.musicplayer.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Size
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.io.IOException
import java.util.Locale
import kotlin.math.ceil
import kotlin.time.Duration

class PlayerActivity : AppCompatActivity() {


    private var musicService: MusicPlayerService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MusicPlayerService.MusicServiceBinder
            musicService = binder.getService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }

    }

    private lateinit var seekBar: SeekBar
    private lateinit var currentPositionTextView: TextView
    private lateinit var duration: TextView
    private lateinit var buttonRepeat: ImageButton
    private lateinit var updateSeekBarJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
//        val serviceIntent = Intent(this, MusicPlayerService::class.java)
//        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
//
//        val currentSong = songViewModel.getSongWithChangedPlayingState()
//        lifecycleScope.launch {
//            loadSongThumbnail(currentSong!!)
//        }
//
//        updateSeekBarJob = MainScope().launch {
//            updateProgressBar(seekBar, currentPositionTextView)
//        }
//
//        val titleTextView = findViewById<TextView>(R.id.songTitlePlayerActivity)
//        titleTextView.text = currentSong?.title ?: "title unknown"
//
//        seekBar = findViewById(R.id.seekBar)
//        seekBar.max = currentSong?.duration?.toInt()!!
//
//        currentPositionTextView = findViewById(R.id.currentPosition)
//
//        duration = findViewById(R.id.songDurationSeekBar)
//        duration.text = formatMilliseconds(currentSong.duration)
//
//        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) {
//
//            }
//
//            override fun onStartTrackingTouch(s: SeekBar?) {
//                if(s?.progress == s?.max) {
//                    //songViewModel.getSongWithChangedPlayingState()?.isPlaying = AudioState.PLAY
//                }
//            }
//
//            override fun onStopTrackingTouch(s: SeekBar?) {
//                musicService?.audioPlayer?.setCurrentPosition(s?.progress?.toLong()!!)
//                lifecycleScope.launch {
//                    updateProgressBar(seekBar, currentPositionTextView)
//                }
//            }
//
//        })
//
//
//        buttonRepeat = findViewById(R.id.btnRepeat)
//        initializeButtonRepeat(buttonRepeat)
    }


//    private suspend fun loadSongThumbnail(currentSong: Song) {
//        withContext(Dispatchers.Main) {
//            try {
//                val thumbnail = currentSong.let {
//                    contentResolver.loadThumbnail(
//                        it.uri,
//                        Size(300,300),
//                        null
//                    )
//                }
//                findViewById<ImageView>(R.id.albumThumbnail).setImageBitmap(thumbnail)
//            } catch (e: IOException) {
//                findViewById<ImageView>(R.id.albumThumbnail).setImageDrawable(ContextCompat.getDrawable(this@PlayerActivity, R.drawable.ic_action_name))
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unbindService(connection)
//        updateSeekBarJob.cancel()
//    }
//
//    private fun initializeButtonRepeat(buttonRepeat: ImageButton) {
//        buttonRepeat.setOnClickListener{
//            if (songViewModel.repeat.value == true) {
//                songViewModel.setRepetition(false)
//                musicService?.audioPlayer?.setLooping(false)
//                buttonRepeat.setColorFilter(getColor(R.color.skyBlue))
//            } else {
//                songViewModel.setRepetition(true)
//                musicService?.audioPlayer?.setLooping(true)
//                buttonRepeat.setColorFilter(getColor(R.color.darkBlue))
//            }
//        }
//    }
//
//    private suspend fun updateProgressBar(seekBar: SeekBar, currentPositionTextView: TextView) {
//        withContext(Dispatchers.Main) {
//            while (seekBar.progress != seekBar.max) {
//                val currentPosition = musicService!!.audioPlayer?.getCurrentPlaybackPosition()
//                seekBar.progress = currentPosition!!.toInt()
//                currentPositionTextView.text = formatMilliseconds(currentPosition)
//                delay(15L)
//                //println("update")
//            }
//        }
//    }
//
//    /**
//     * Function allows to change milliseconds to minutes and seconds
//     * @param milliseconds song duration in milliseconds
//     * @return duration in format M.SS (e.g. 2.43)
//     */
//    private fun formatMilliseconds(milliseconds: Long): String {
//        val seconds = ceil(milliseconds / 1000.0)
//        val minutes = (seconds / 60).toInt()
//        val remainingSeconds = seconds % 60
//
//        return String.format(Locale.getDefault(), "%d:%02.0f", minutes, remainingSeconds)
//    }

}