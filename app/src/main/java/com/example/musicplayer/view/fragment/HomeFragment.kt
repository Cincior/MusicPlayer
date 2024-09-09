package com.example.musicplayer.view.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.databinding.FragmentHomeBinding
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.MainActivity
import com.example.musicplayer.viewmodel.SongViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    companion object {
        lateinit var songAdapter: SongAdapter
        private const val EXTRA_ITEM_INDEX = "EXTRA_ITEM_ID"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val songViewModel: SongViewModel by activityViewModels()

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

        songAdapter = SongAdapter(songViewModel.items.value!!)
        initializeAdapterOnClickFunctions(songAdapter)

        (binding.songList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.songList.adapter = songAdapter

        initializeSwipeRefreshLayout()

        initializeSearchViewOnActionListener(binding.searchSong)

        songViewModel.currentSong.observe(viewLifecycleOwner) {
            manageSong(songAdapter)
        }

//        songViewModel.items.observe(viewLifecycleOwner) { items ->
//
//        }

        binding.btnPlaylists.setOnClickListener {
            //findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
        }

        binding.btnFavourites.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favouritesFragment)
        }

        binding.settingBtn.setOnClickListener {
            Toast.makeText(requireContext(), "There will be settings (choosing folder with song)", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                                val intentExtraId = Intent().apply {
                                    println(position.toString() + "pozycja przed wysalniem")
                                    putExtra(EXTRA_ITEM_INDEX, position)
                                }

                                intentSenderLauncher.launch(
                                    IntentSenderRequest.Builder(deleteRequest)
                                        .setFillInIntent(intentExtraId)
                                        .build()
                                )
//                                deletionId = song.id.toInt()
//                                println(position.toString() + "???" + deletionId)
//                                listId = position
                            }

                            1 -> {
                                val activity = requireActivity() as MainActivity
                                lifecycleScope.launch {
                                    if (songViewModel.favouritesRepository.isInFavourites(song.id.toString())) {
                                        activity.createSnackBar("Song is already in favourites!", Gravity.TOP).show()
                                    } else {
                                        songViewModel.favouritesRepository.addSongToFavourites(song.id.toString())
                                        activity.createSnackBar("Added to favourites!", Gravity.TOP).show()
                                    }
                                }
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
        songAdapter: SongAdapter
    ) {
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
            val activity = requireActivity() as MainActivity
            val snackbar = activity.createSnackBar("Refresh completed!", Gravity.TOP)
            snackbar.show()

        }
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
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val deletionData = result.data
                    if (deletionData != null) {
                        val deletionIndex = deletionData.getIntExtra(EXTRA_ITEM_INDEX, -1)
                        println(deletionIndex.toString() + "tutaj")
                        if (deletionIndex != -1) {
                            if (songViewModel.currentSong.value?.id?.toInt() == deletionIndex) {
                                musicService?.audioPlayer?.destroyPlayer()
                            }
                            songViewModel.deleteSong(deletionIndex)
                            songAdapter.updateAfterDeletion(
                                deletionIndex,
                                songViewModel.getSongsCount(),
                            )
                        }
                    } else {
                        println(deletionData)
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