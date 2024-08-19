package com.example.musicplayer.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer(private val context: Context, private val uri: Uri) {
    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    fun playSong() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
            start()
            setOnCompletionListener {
                onCompletionListener?.invoke()
            }
        }
    }

    fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun pauseSong() {
        mediaPlayer?.pause()
    }

    fun resumeSong() {
        mediaPlayer?.start()
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        this.onCompletionListener = listener
    }

}