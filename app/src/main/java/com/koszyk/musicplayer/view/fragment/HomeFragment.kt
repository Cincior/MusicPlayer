package com.koszyk.musicplayer.view.fragment

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.adapters.SongAdapter
import com.koszyk.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.koszyk.musicplayer.databinding.FragmentHomeBinding
import com.koszyk.musicplayer.media.MusicPlayerService
import com.koszyk.musicplayer.model.AudioState
import com.koszyk.musicplayer.model.Song
import com.koszyk.musicplayer.view.MainActivity
import com.koszyk.musicplayer.viewmodel.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    companion object {
        lateinit var songAdapter: SongAdapter
        private const val EXTRA_ITEM_INDEX = "EXTRA_ITEM_ID"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var deletionId = -1 // Id of particular Song that can be deleted
    var listId = -1 // Id of item in recyclerView that can be deleted
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

        songViewModel.isSongsLoaded.observe(viewLifecycleOwner) { isLoaded ->
            if (isLoaded) {
                binding.homeProgressIndicator.visibility = View.GONE
                if (songViewModel.items.value.isNullOrEmpty()) {
                    binding.homeNoSongWarning.visibility = View.VISIBLE
                }
                else {
                    binding.homeNoSongWarning.visibility = View.GONE
                }
                songAdapter = SongAdapter(songViewModel.items.value!!)
                initializeAdapterOnClickFunctions(songAdapter)

                if (binding.searchSong.query.isNotEmpty()) {
                    songAdapter.filterSongs(binding.searchSong.query.toString())
                }

                (binding.songList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                binding.songList.adapter = songAdapter

                initializeSwipeRefreshLayout()

                initializeSearchViewOnActionListener(binding.searchSong)
                songViewModel.currentSong.observe(viewLifecycleOwner) {
                    manageSong(songAdapter)
                }

                songViewModel.isCheckedStateChanged.observe(viewLifecycleOwner) { state ->
                    if (state == true) {
                        refreshSongs(binding.swipeRecyclerViewLayout)
                        songViewModel.changeIsCheckedState(false)
                    }
                }
            }
        }



        binding.btnPlaylists.setOnClickListener {
            //findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
        }

        binding.btnFavourites.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favouritesFragment)
        }

        binding.settingBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
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
                                intentSenderLauncher.launch(
                                    IntentSenderRequest.Builder(deleteRequest).build()
                                )
                                deletionId = song.id.toInt()
                                listId = position
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
            refreshSongs(swipeRefreshLayout)
        }
    }

    private fun refreshSongs(swipeRefreshLayout: SwipeRefreshLayout) {

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                swipeRefreshLayout.isRefreshing = true;
            }
            val existingQuery = binding.searchSong.query.toString()
            songViewModel.getSongsFromChosenFoldersSuspend(requireContext())

            withContext(Dispatchers.Main) {
                if (songViewModel.currentSong.value == null) {
                    musicService?.audioPlayer?.destroyPlayer()
                }
                songAdapter.insertNewItems(songViewModel.items.value!!)
                if (existingQuery.isNotEmpty()) {
                    songAdapter.filterSongs(existingQuery)
                }
                songAdapter.notifyItemRangeChanged(0, songViewModel.getSongsCount() - 1)
                swipeRefreshLayout.isRefreshing = false;
                val activity = requireActivity() as MainActivity
                val snackbar = activity.createSnackBar("Refresh completed!", Gravity.TOP)
                snackbar.show()
            }
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
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (listId != -1) {
                        if (songViewModel.currentSong.value?.id == deletionId.toLong()) {
                            musicService?.audioPlayer?.destroyPlayer()
                        }
                        songViewModel.deleteSong(listId)
                        songAdapter.updateAfterDeletion(
                            listId,
                            songViewModel.items.value!!.size,
                        )
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