package com.koszyk.musicplayer.media

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.media.NotificationChannel.Companion.NOTIFICATION_CHANNEL
import com.koszyk.musicplayer.view.MainActivity
import com.koszyk.musicplayer.view.MainActivity.Companion.EXTRA_ARTIST
import com.koszyk.musicplayer.view.MainActivity.Companion.EXTRA_NOTIFICATION_SERVICE
import com.koszyk.musicplayer.view.MainActivity.Companion.EXTRA_TITLE

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
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "song unknown"
        val artist = intent?.getStringExtra(EXTRA_ARTIST) ?: "artist unknown"
        when (intent?.action) {
            Actions.Start.toString() -> start(title, artist)
            Actions.Stop.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(title: String, artist: String) {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.putExtra(EXTRA_NOTIFICATION_SERVICE, true)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle(title)
            .setContentInfo("desc")
            .setContentText(artist)
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

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                println("cringe call")
            }
        }
    }

}