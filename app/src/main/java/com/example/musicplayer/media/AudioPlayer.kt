package com.example.musicplayer.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast

class AudioPlayer(private val context: Context, private val uri: Uri, private val isLoopEndabled: Boolean) {
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
            isLooping = isLoopEndabled
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

    fun setLooping(isEnabled: Boolean) {
        mediaPlayer?.isLooping = isEnabled
        Toast.makeText(context, mediaPlayer?.isLooping.toString(), Toast.LENGTH_SHORT).show()
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        this.onCompletionListener = listener
    }


}