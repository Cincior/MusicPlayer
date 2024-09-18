package com.koszyk.musicplayer.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.databinding.ActivityMainBinding
import com.koszyk.musicplayer.media.MusicPlayerService
import com.koszyk.musicplayer.model.AudioState
import com.koszyk.musicplayer.model.Song
import com.koszyk.musicplayer.view.mainActivityHelpers.*
import com.koszyk.musicplayer.viewmodel.SongViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_NOTIFICATION_SERVICE = "notificationService"
        var permissionGranted = false
    }
    private lateinit var binding: ActivityMainBinding
    private var actionToPlayer = R.id.action_homeFragment_to_playerFragment

    private val songViewModel: SongViewModel by viewModels()

    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                musicService?.audioPlayer?.pauseSong()
                songViewModel.changeCurrentSongStateAfterAudioFocusLost()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss of audio focus
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume or duck the audio
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Regain focus, resume playback
            }
        }
    }

    var musicService: MusicPlayerService? = null
    private lateinit var navController: NavController
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicServiceBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MusicPlayerService::class.java))
        unbindService(connection)
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(binding.root)
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHostFragment.navController


        // GET PERMISSIONS FIRST
        getPermission(this)
        if (!permissionGranted) {
            finish()
        }

        // Get songs once the app is being created
        lifecycleScope.launch {
            songViewModel.getSongsFromChosenFoldersSuspend(this@MainActivity)
        }
        songViewModel.initializeRepo(this)

        songViewModel.currentSong.observe(this) { s ->
            if (s != null) {
                if (s.isPlaying != AudioState.PAUSE) {
                    audioManager.requestAudioFocus(focusRequest)
                }
                changeBottomPlayer(s)
                manageSong(s)
            } else {
                binding.songTitleBottom.text = getString(R.string.chose_your_song)
            }
        }

        binding.btnPlayPauseBottom.setOnClickListener {
            songViewModel.changeCurrentSongState()
        }

        binding.titleSectionBottom.setOnClickListener {
            if (songViewModel.currentSong.value != null) {
                navController.navigate(actionToPlayer)
            } else {
                val sB = createSnackBar("Choose your song first!", null)
                sB.show()
            }

        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.playerFragment -> {
                    binding.bottomPlayerManager.visibility = View.GONE
                }
                R.id.favouritesFragment -> {
                    actionToPlayer = R.id.action_favouritesFragment_to_playerFragment
                    binding.bottomPlayerManager.visibility = View.VISIBLE
                }
                R.id.homeFragment -> {
                    actionToPlayer = R.id.action_homeFragment_to_playerFragment
                    binding.bottomPlayerManager.visibility = View.VISIBLE
                }
                R.id.settingsFragment -> {
                    actionToPlayer = R.id.action_settingsFragment_to_playerFragment
                    binding.bottomPlayerManager.visibility = View.VISIBLE
                }
                R.id.topListFragment -> {
                    actionToPlayer = R.id.action_topListFragment_to_playerFragment
                    binding.bottomPlayerManager.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val isFromNotification = intent.getBooleanExtra(EXTRA_NOTIFICATION_SERVICE, false)
        if (isFromNotification) {
            navController.navigate(R.id.homeFragment)
        }
    }

    private fun changeBottomPlayer(song: Song) {
        binding.songTitleBottom.text = song.title
        if (song.isPlaying == AudioState.PLAY || song.isPlaying == AudioState.RESUME) {
            binding.btnPlayPauseBottom.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_btn))
        } else {
            binding.btnPlayPauseBottom.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_btn))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeReadMemory) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                permissionGranted = true
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                permissionGranted = false
            }
        }
    }

    private fun manageSong(
        song: Song,
    ) {
        when (song.isPlaying) {
            AudioState.PAUSE -> {
                musicService?.audioPlayer?.pauseSong()
            }
            AudioState.PLAY -> {
                Intent(this, MusicPlayerService::class.java).also {
                    it.putExtra(EXTRA_TITLE, song.title)
                    it.putExtra(EXTRA_ARTIST, song.artist)
                    it.action = MusicPlayerService.Actions.Start.toString()
                    startService(it)
                }
                musicService?.audioPlayer?.playSong(song.uri, songViewModel.repeat.value ?: false) {
                    if (songViewModel.currentSong.value != null) {
                        songViewModel.currentSong.value?.isPlaying = AudioState.END
                        songViewModel.currentSong.value?.let { songViewModel.updateCurrentSong(it) }
                    } else {
                        binding.songTitleBottom.text = "CHOOSE YOUR SONG"
                    }

                }
            }
            AudioState.RESUME -> {
                musicService?.audioPlayer?.resumeSong()
            }
            else ->{
                println("managing in main activity error")
            }
        }

    }

    fun createSnackBar(message: String, gravity: Int?): Snackbar {
        val snackbar = Snackbar.make(
            this,
            binding.main,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
        if (gravity != null) {
            val snackbarParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
            snackbarParams.gravity = gravity
            snackbar.view.layoutParams = snackbarParams
        }

        return snackbar
    }
}