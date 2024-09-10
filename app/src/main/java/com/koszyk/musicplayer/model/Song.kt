package com.koszyk.musicplayer.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    var image: String,
    var isPlaying: AudioState
)

enum class AudioState {
    PLAY,
    PAUSE,
    RESUME,
    END,
    NONE
}
