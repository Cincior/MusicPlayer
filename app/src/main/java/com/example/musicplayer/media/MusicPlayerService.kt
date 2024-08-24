package com.example.musicplayer.media

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicplayer.R

class MusicPlayerService: Service() {



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "song unknown"
        when(intent?.action) {
            Actions.Start.toString() -> start(title)
            Actions.Stop.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(title: String) {
        val notification = NotificationCompat.Builder(this, "music-player-channel")
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle(title)
            .setContentInfo("desc")
            .setContentText("chuj")
            .build()
        startForeground(1, notification)
    }

    enum class Actions {
        Start,
        Stop
    }
}