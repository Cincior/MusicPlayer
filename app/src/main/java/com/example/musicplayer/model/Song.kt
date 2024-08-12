package com.example.musicplayer.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: String,
    val uri: Uri,
    var isPlaying: AudioState
)

enum class AudioState {
    PLAY,
    PAUSE,
    NONE
}
