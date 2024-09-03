package com.example.musicplayer.media

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.MainActivity

class MusicPlayerService : Service() {
    var audioPlayer: AudioPlayer? = null
    private val binder = MusicServiceBinder()

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        audioPlayer = AudioPlayer(applicationContext)
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "song unknown"
        when (intent?.action) {
            Actions.Start.toString() -> start(title)
            Actions.Stop.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(title: String) {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.putExtra("notificationService", true)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "music-player-channel")
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle(title)
            .setContentInfo("desc")
            .setContentText("")
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setColor(ContextCompat.getColor(this, R.color.darkBlue))
            .build()
        startForeground(1, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
        audioPlayer?.destroyPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer?.destroyPlayer()
    }

    enum class Actions {
        Start,
        Stop
    }

}