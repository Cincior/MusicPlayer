package com.example.musicplayer.media

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayer(private val context: Context) {
    private var mediaExoPlayer = ExoPlayer.Builder(context).build()

    fun playSong(uri: Uri, isLoopEnabled: Boolean, onCompletionListener: (() -> Unit)? = null) {
        if (mediaExoPlayer.isPlaying) {
            mediaExoPlayer.stop()
            //mediaExoPlayer.clearMediaItems()
        }
        val mediaItem = MediaItem.fromUri(uri)
        mediaExoPlayer.setMediaItem(mediaItem)
        mediaExoPlayer.repeatMode = if (isLoopEnabled) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
        mediaExoPlayer.prepare()
        mediaExoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (ExoPlayer.STATE_ENDED == playbackState) {
                    onCompletionListener?.invoke()
                }
            }
        })
        mediaExoPlayer.play()
    }

    fun stopSong() {
        mediaExoPlayer.stop()
    }

    fun pauseSong() {
        mediaExoPlayer.pause()
    }

    fun resumeSong() {
        mediaExoPlayer.play()
    }

    fun setLooping(isEnabled: Boolean) {
        mediaExoPlayer.repeatMode = if (isEnabled) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
        Toast.makeText(context, mediaExoPlayer.repeatMode.toString(), Toast.LENGTH_SHORT).show()
    }

    fun destroyPlayer() {
        mediaExoPlayer.stop()
        mediaExoPlayer.clearMediaItems()
    }

}