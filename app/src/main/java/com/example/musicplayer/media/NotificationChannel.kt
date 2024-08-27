package com.example.musicplayer.media

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class NotificationChannel: Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "music-player-channel",
            "Music notifications",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}