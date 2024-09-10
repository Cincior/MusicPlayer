package com.koszyk.musicplayer.media

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class NotificationChannel: Application() {
    companion object {
        const val NOTIFICATION_CHANNEL = "music-player-channel"
    }
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            "Music notifications",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}