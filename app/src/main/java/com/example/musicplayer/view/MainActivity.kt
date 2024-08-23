package com.example.musicplayer.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.media.AudioPlayerManager
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.fragment.PlayingManagerFragment
import com.example.musicplayer.view.mainActivityHelpers.*
import com.example.musicplayer.viewmodel.SongViewModel
import com.example.musicplayer.viewmodel.ViewModelSingleton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    companion object {
        var permissionGranted = false
    }
    var deletionId = -1 // Id of particular Song that can be deleted
    var listId = -1 // Id of item in recyclerView that can be deleted
    private var setBottomHeight = false
    private var audioPlayerManager: AudioPlayerManager? = null

    private val songViewModel: SongViewModel by lazy {
        ViewModelSingleton.getSharedViewModel(application)
    }
    private lateinit var songAdapter: SongAdapter
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> // To ask user about deletion of song

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(R.layout.activity_main)

        // GET PERMISSIONS FIRST
        getPermission(this)
        if (!permissionGranted) {
            finish()
        }

        registerIntentSender()

        val recyclerView = findViewById<RecyclerView>(R.id.songList)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        //songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.getSongs()

        songAdapter = SongAdapter(songViewModel.items.value!!)
        initializeAdapterOnClickFunctions(songAdapter)
        recyclerView.adapter = songAdapter

        initializeSwipeRefreshLayout()

        searchView = findViewById(R.id.searchSong)
        initializeSearchViewOnActionListener(searchView)

        audioPlayerManager = AudioPlayerManager(this@MainActivity, songViewModel, songAdapter)
        songViewModel.repeat.observe(this) { state ->
            audioPlayerManager!!.changeIsLooping(state)
        }

        findViewById<ExtendedFloatingActionButton>(R.id.btnFavourites).setOnClickListener{
            val intent = Intent(this@MainActivity, FavouritesActivity::class.java)
            startActivity(intent)
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                audioPlayerManager?.destroyPlayer()
                finish()
            }
        })
    }

    override fun onDestroy() {
        Toast.makeText(this@MainActivity, "po robocie", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

    private fun registerIntentSender() {
        // Registering deletion
        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (deletionId != -1) {
                    songViewModel.deleteSong(deletionId.toLong())
                    if (songViewModel.getSongWithChangedPlayingState()?.id == deletionId.toLong()) {
                        audioPlayerManager?.destroyPlayer()
                    }
                    songAdapter.updateAfterDeletion(listId, songViewModel.items.value!!.size, deletionId.toLong())
                    deletionId = -1
                    listId = -1
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Song couldn't be deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
        Toast.makeText(this@MainActivity, "resume", Toast.LENGTH_SHORT).show()
        val s = songViewModel.items.value?.find {
            it.isPlaying == AudioState.PLAY
        }
        println("resume: " + s)
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

    private fun initializeAdapterOnClickFunctions(songAdapter: SongAdapter)
    {
        // Passing to adapter implemented functions of interface
        songAdapter.setOnClickListener(object : SongAdapter.IonClickListener {
            /**
             * Function lets user open menu from which the user can delete an audio file.
             * @param position position of clicked song
             * @param song particular Song object that has been clicked
             */
            override fun onLongClick(position: Int, song: Song) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder
                    .setTitle(song.title)
                    .setItems(arrayOf("Delete", "Add to favourites", "Cancel")) { _, which ->
                        when(which) {
                            0 -> {
                                val deleteRequest = MediaStore.createDeleteRequest(
                                    contentResolver,
                                    listOf(song.uri)
                                )
                                intentSenderLauncher.launch(
                                    IntentSenderRequest.Builder(deleteRequest).build()
                                )
                                deletionId = song.id.toInt()
                                listId = position
                            }
                            1 -> {
                                Toast.makeText(this@MainActivity, "Added to favourites (not yet implemented)", Toast.LENGTH_SHORT).show()
                            }
                            else -> {

                            }
                        }
                    }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            /**
             * Function responsible for playing or stopping music
             * @param holder contains whole song UI elements
             * @param song clicked Song object
             */
            override fun onClick(holder: ItemViewHolder, song: Song) {
                when (song.isPlaying) {
                    AudioState.PLAY -> audioPlayerManager?.pauseSong(song, holder)
                    AudioState.PAUSE -> audioPlayerManager?.resumeSong(song, holder)
                    else -> {
                        audioPlayerManager?.playSong(song)
                    }
                }
                initializeFragment()
            }
        })
    }

    private fun showBottomLayout() {
        if(!setBottomHeight)
        {
            val dpHeight = 70 // height in dp
            val density = resources.displayMetrics.density
            val heightInPx = (dpHeight * density).toInt()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightInPx
            )
            findViewById<LinearLayout>(R.id.bottomLinearLayout).layoutParams = params
        }
    }

    private fun initializeFragment() {

        val fragment = PlayingManagerFragment.newInstance("", "")
        fragment.setActionListener(object : PlayingManagerFragment.IonActionListener {
            override fun onButtonPlayPauseClicked() {
                val currentSong = songViewModel.getSongWithChangedPlayingState()
                when (currentSong?.isPlaying) {
                    AudioState.PLAY -> audioPlayerManager?.pauseSongFragment(currentSong)
                    AudioState.PAUSE -> audioPlayerManager?.resumeSongFragment(currentSong)
                    else -> audioPlayerManager?.playSong(currentSong!!)
                }
            }
        })
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
        showBottomLayout()
    }

    private fun initializeSearchViewOnActionListener(searchView: SearchView)
    {
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                onQueryTextChange(query)
                searchView.clearFocus()
                return true
            }
            override fun onQueryTextChange(query: String?): Boolean {
                val safeQuery = query ?: ""
                songAdapter.filterSongs(safeQuery)
                return true
            }
        })

    }


//    fun hideKeyboard() {
//        val view = this.currentFocus
//        if (view != null) {
//            val imm = getSystemService(this@MainActivity.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(view.windowToken, 0)
//        }
//    }

    private fun initializeSwipeRefreshLayout() {
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRecyclerViewLayout)
        swipeRefreshLayout.setOnRefreshListener {
            val existingQuery: String = findViewById<SearchView>(R.id.searchSong).query.toString()

            songViewModel.getSongsUpdate()
            songAdapter.insertNewItems(songViewModel.items.value!!)
            if (existingQuery.isNotEmpty()) {
                songAdapter.filterSongs(existingQuery)
            }

            songAdapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false;
            val snackbar = createSnackBar()
            snackbar.show()

        }
    }

    private fun createSnackBar(): Snackbar {
        val snackbar = Snackbar.make(this, findViewById(R.id.fragment_container_view), "Refresh completed!", Snackbar.LENGTH_SHORT)
        snackbar.setAction("OK") {
            snackbar.dismiss()
        }
        val snackbarParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        snackbarParams.gravity = Gravity.TOP
        snackbar.view.layoutParams = snackbarParams
        return snackbar
    }


}