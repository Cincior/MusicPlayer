package com.example.musicplayer.media

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SongService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}