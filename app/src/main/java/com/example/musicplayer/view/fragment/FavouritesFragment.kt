package com.example.musicplayer.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.databinding.FragmentFavouritesBinding
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FavouritesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by activityViewModels()

    private lateinit var songAdapter: SongAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ContentLoadingProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
//        runBlocking {
//            songViewModel.favouritesRepository.deleteAllFavourites()
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songAdapter = HomeFragment.songAdapter
        recyclerView = binding.recyclerViewFavourites
        progressBar = binding.contentLoadingProgressBar
        progressBar.show()

        lifecycleScope.launch {
            delay(10)
            val fav = songViewModel.favouritesRepository.getFavouriteSongs().first()
            songAdapter.filterFavouriteSongs(fav)
            recyclerView.adapter = songAdapter
            withContext(Dispatchers.Main) {
                progressBar.hide()
                //recyclerView.visibility = View.VISIBLE
            }
        }

        songViewModel.currentSong.observe(viewLifecycleOwner) {
            songAdapter.notifyDataSetChanged()
        }

    }

}