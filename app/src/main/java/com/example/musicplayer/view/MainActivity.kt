package com.example.musicplayer.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.repository.FavouritesRepository
import com.example.musicplayer.view.mainActivityHelpers.*
import com.example.musicplayer.viewmodel.SongViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        var permissionGranted = false
    }
    private lateinit var binding: ActivityMainBinding

    private val songViewModel: SongViewModel by viewModels()

    private lateinit var textViewTitle: TextView
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var titleSectionBottom: LinearLayout
    private lateinit var bottomPlayerManager: LinearLayout

    private var actionToPlayer = R.id.action_homeFragment_to_playerFragment


    private var musicService: MusicPlayerService? = null
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(binding.root)
        Toast.makeText(this, "OnCreate", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHostFragment.navController


        // GET PERMISSIONS FIRST
        getPermission(this)
        if (!permissionGranted) {
            finish()
        }

        // Get songs once the app is being created
        songViewModel.getSongs(this)
        songViewModel.initializeRepo(this)

        textViewTitle = binding.songTitleBottom
        buttonPlayPause = binding.btnPlayPauseBottom
        titleSectionBottom = binding.titleSectionBottom
        bottomPlayerManager = binding.bottomPlayerManager

        songViewModel.currentSong.observe(this) { s ->
            changeBottomPlayer(s)
            manageSong(s)
        }

        buttonPlayPause.setOnClickListener {
            songViewModel.changeCurrentSongState()
        }

        titleSectionBottom.setOnClickListener {
            if (songViewModel.currentSong.value != null) {
                navController.navigate(actionToPlayer)
            } else {
                val sB = createSnackBar("Choose your song first!")
                sB.show()
            }

        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.playerFragment -> {
                    bottomPlayerManager.visibility = View.GONE
                }
                R.id.favouritesFragment -> {
                    actionToPlayer = R.id.action_favouritesFragment_to_playerFragment
                    bottomPlayerManager.visibility = View.VISIBLE
                }
                R.id.homeFragment -> {
                    actionToPlayer = R.id.action_homeFragment_to_playerFragment
                    bottomPlayerManager.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val isFromNotification = intent.getBooleanExtra("notificationService", false)
        if (isFromNotification) {
            navController.navigate(R.id.homeFragment)
        }
    }

    private fun changeBottomPlayer(song: Song) {
        textViewTitle.text = song.title
        if (song.isPlaying == AudioState.PLAY || song.isPlaying == AudioState.RESUME) {
            buttonPlayPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_btn))
        } else {
            buttonPlayPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_btn))
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
                Toast.makeText(this, "Odmowa uprawnien", Toast.LENGTH_SHORT).show()
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
                    it.putExtra("title", song.title)
                    it.putExtra("artist", song.artist)
                    it.action = MusicPlayerService.Actions.Start.toString()
                    startService(it)
                }
                musicService?.audioPlayer?.playSong(song.uri, songViewModel.repeat.value ?: false) {
                    songViewModel.currentSong.value?.isPlaying = AudioState.END
                    songViewModel.updateCurrentSong(songViewModel.currentSong.value!!)
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

    fun createSnackBar(message: String): Snackbar {
        val snackbar = Snackbar.make(
            this,
            binding.main,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
        val snackbarParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        snackbarParams.gravity = Gravity.TOP
        snackbar.view.layoutParams = snackbarParams
        return snackbar
    }
}