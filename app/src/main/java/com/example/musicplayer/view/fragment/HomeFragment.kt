package com.example.musicplayer.view.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.provider.MediaStore
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.databinding.FragmentHomeBinding
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.MainActivity
import com.example.musicplayer.viewmodel.SongViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var deletionId = -1 // Id of particular Song that can be deleted
    var listId = -1 // Id of item in recyclerView that can be deleted
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val songViewModel: SongViewModel by activityViewModels()
    private lateinit var songAdapter: SongAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var musicService: MusicPlayerService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicServiceBinder
            musicService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerIntentSender()

        recyclerView = binding.songList
        searchView = binding.searchSong

        songAdapter = SongAdapter(songViewModel.items.value!!)
        initializeAdapterOnClickFunctions(songAdapter)

        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = songAdapter

        initializeSwipeRefreshLayout()

        initializeSearchViewOnActionListener(searchView)

        songViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            println("Home fragment triggered")
            manageSong(song, songAdapter)
        }

        binding.btnPlaylists.setOnClickListener{
            //findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
        }

    }

    override fun onResume() {
        super.onResume()
    }

    private fun initializeAdapterOnClickFunctions(songAdapter: SongAdapter) {
        // Passing to adapter implemented functions of interface
        songAdapter.setOnClickListener(object : SongAdapter.IonClickListener {
            /**
             * Function lets user open menu from which the user can delete an audio file.
             * @param position position of clicked song
             * @param song particular Song object that has been clicked
             */
            override fun onLongClick(position: Int, song: Song) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder
                    .setTitle(song.title)
                    .setItems(arrayOf("Delete", "Add to favourites", "Cancel")) { _, which ->
                        when (which) {
                            0 -> {
                                val deleteRequest = MediaStore.createDeleteRequest(
                                    requireContext().contentResolver,
                                    listOf(song.uri)
                                )
                                intentSenderLauncher.launch(
                                    IntentSenderRequest.Builder(deleteRequest).build()
                                )
                                deletionId = song.id.toInt()
                                listId = position
                            }

                            1 -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Added to favourites (not yet implemented)",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                songViewModel.updateCurrentSongState(song)
                songViewModel.updateCurrentSong(song)
            }
        })
    }

    private fun manageSong(
        song: Song,
        songAdapter: SongAdapter
    ) {
        println("wywoluje sie: " + songViewModel.currentSong.value?.isPlaying)
        songAdapter.notifyDataSetChanged()
    }

    private fun initializeSwipeRefreshLayout() {
        val swipeRefreshLayout = binding.swipeRecyclerViewLayout
        swipeRefreshLayout.setOnRefreshListener {
            val existingQuery = binding.searchSong.query.toString()

            songViewModel.getSongsUpdate(requireContext())
            songAdapter.insertNewItems(songViewModel.items.value!!)
            if (existingQuery.isNotEmpty()) {
                songAdapter.filterSongs(existingQuery)
            }

            songAdapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false;
            val snackbar = createSnackBar("Refresh completed!")
            snackbar.show()

        }
    }

    private fun createSnackBar(message: String): Snackbar {
        val snackbar = Snackbar.make(
            requireContext(),
            binding.homeMain,
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

    private fun initializeSearchViewOnActionListener(searchView: SearchView) {
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

    private fun registerIntentSender() {
        // Registering deletion
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (deletionId != -1) {
                        songViewModel.deleteSong(deletionId.toLong())
                        if (songViewModel.currentSong.value?.id == deletionId.toLong()) {
                            musicService?.audioPlayer?.destroyPlayer()
                            val itemsCount = songViewModel.getSongsCount()
                            if (itemsCount > 1) {
                                val newSong: Song
                                if (listId == itemsCount - 1) {
                                    newSong = songViewModel.items.value?.get(0)!!
                                } else {
                                    newSong = songViewModel.items.value?.get(listId)!!
                                }
                                newSong.isPlaying = AudioState.PLAY
                                songViewModel.updateCurrentSong(newSong)
                            } else {
                                TODO("what if there is 1 song")
                            }
                        }
                        songAdapter.updateAfterDeletion(
                            listId,
                            songViewModel.items.value!!.size,
                            deletionId.toLong()
                        )
                        deletionId = -1
                        listId = -1
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Song couldn't be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}