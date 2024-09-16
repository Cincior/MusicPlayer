package com.koszyk.musicplayer.view.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.databinding.FragmentPlayerBinding
import com.koszyk.musicplayer.media.MusicPlayerService
import com.koszyk.musicplayer.model.AudioState
import com.koszyk.musicplayer.viewmodel.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.math.ceil

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val songViewModel: SongViewModel by activityViewModels()

    private var musicService: MusicPlayerService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicServiceBinder
            musicService = binder.getService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }

    }

    private lateinit var seekBar: SeekBar
    private lateinit var currentPositionTextView: TextView
    private lateinit var duration: TextView
    private lateinit var titleTextView: TextView
    private lateinit var buttonRepeat: ImageButton
    private lateinit var songThumbnail: ImageView
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var buttonPrevious: ImageButton
    private lateinit var buttonNext: ImageButton
    private lateinit var buttonFavourite: ImageButton
    private lateinit var favSongs: Set<String>

    private lateinit var imageButtonAnimation: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        imageButtonAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.image_button_click_anim)

        runBlocking {
            favSongs = songViewModel.favouritesRepository.getFavouriteSongs().first()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = binding.seekBarSongCurrentPosition
        currentPositionTextView = binding.currentPosition
        duration = binding.songDurationSeekBar
        titleTextView = binding.songTitlePlayerFragment
        buttonRepeat = binding.btnRepeat
        songThumbnail = binding.albumThumbnail
        buttonPlayPause = binding.btnPlayPausePlayerFragment
        buttonPrevious = binding.btnPreviousPlayerFragment
        buttonNext = binding.btnNextPlayerFragment
        buttonFavourite = binding.btnAddToFavourites

        setSongData()

        buttonRepeat.setOnClickListener {
            songViewModel.toggleRepetition()
        }

        songViewModel.repeat.observe(viewLifecycleOwner) {
            toggleRepetition()
        }

        songViewModel.currentSong.observe(viewLifecycleOwner) { cSong ->
            if (cSong.isPlaying in listOf(
                    AudioState.PLAY,
                    AudioState.RESUME)
            ) {
                buttonPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause_circle))
            } else {
                buttonPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_circle))
            }
            lifecycleScope.launch {
                updateSeekbar()
            }
            if (songViewModel.currentSong.value?.id.toString() in favSongs) {
                buttonFavourite.setColorFilter(getColor(requireContext(), R.color.red))
            } else {
                buttonFavourite.setColorFilter(getColor(requireContext(), R.color.white))
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(s: SeekBar?) {

            }

            override fun onStopTrackingTouch(s: SeekBar) {
                if (songViewModel.currentSong.value?.isPlaying == AudioState.END) {
                    songViewModel.changeCurrentSongState()
                }
                musicService?.audioPlayer?.setCurrentPosition(s.progress.toLong())
            }
        })

        buttonPlayPause.setOnClickListener {
            songViewModel.changeCurrentSongState()
        }

        buttonNext.setOnClickListener {
            songViewModel.setCurrentSongNext()
            setSongData()
            it.startAnimation(imageButtonAnimation)
        }

        buttonPrevious.setOnClickListener {
            songViewModel.setCurrentSongPrev()
            setSongData()
            it.startAnimation(imageButtonAnimation)
        }

        buttonFavourite.setOnClickListener {
            //TODO(change that anim)
            it.startAnimation(imageButtonAnimation)
            lifecycleScope.launch {
                if (songViewModel.currentSong.value?.id.toString() in favSongs) {
                    songViewModel.favouritesRepository.deleteSongFromFavourites(songViewModel.currentSong.value?.id.toString())
                    buttonFavourite.setColorFilter(getColor(requireContext(), R.color.white))
                } else {
                    songViewModel.favouritesRepository.addSongToFavourites(songViewModel.currentSong.value?.id.toString())
                    buttonFavourite.setColorFilter(getColor(requireContext(), R.color.red))
                }
                favSongs = songViewModel.favouritesRepository.getFavouriteSongs().first()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSongData() {
        lifecycleScope.launch {
            loadThumbnail(songThumbnail)
        }

        titleTextView.text = songViewModel.currentSong.value?.title ?: "Unknown title"

        duration.text = formatMilliseconds(songViewModel.currentSong.value?.duration ?: 0)

        seekBar.max = songViewModel.currentSong.value?.duration?.toInt() ?: 0
    }

    override fun onResume() {
        super.onResume()
        if (songViewModel.currentSong.value?.isPlaying == AudioState.END) {
            seekBar.progress = seekBar.max
            currentPositionTextView.text = formatMilliseconds(seekBar.max.toLong())
        }
    }

    private suspend fun loadThumbnail(songThumbnail: ImageView) {
        withContext(Dispatchers.Main) {
            try {
                val thumbnail = songViewModel.currentSong.value?.let {
                    requireContext().contentResolver.loadThumbnail(
                        it.uri,
                        Size(500, 500),
                        null
                    )
                }
                songThumbnail.setImageBitmap(thumbnail)
            } catch (e: IOException) {
                songThumbnail.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_note_music
                    )
                )
            }
        }
    }

    /**
     * Function allows to change milliseconds to minutes and seconds
     * @param milliseconds song duration in milliseconds
     * @return duration in format M.SS (e.g. 2.43)
     */
    private fun formatMilliseconds(milliseconds: Long): String {
        val seconds = ceil(milliseconds / 1000.0)
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = seconds % 60

        return String.format(Locale.getDefault(), "%d:%02.0f", minutes, remainingSeconds)
    }

    private fun toggleRepetition() {
        if (songViewModel.repeat.value == true) {
            musicService?.audioPlayer?.setLooping(true)
            buttonRepeat.setColorFilter(getColor(requireContext(), R.color.skyBlue))
        } else {
            musicService?.audioPlayer?.setLooping(false)
            buttonRepeat.setColorFilter(getColor(requireContext(), R.color.white))
        }
    }

    private suspend fun updateSeekbar() {
        withContext(Dispatchers.IO) {
            do {
                withContext(Dispatchers.Main) {
                    val currentPosition =
                        musicService?.audioPlayer?.getCurrentPlaybackPosition()?.toInt()!!
                    if (seekBar.max >= currentPosition) {
                        seekBar.progress = currentPosition
                        currentPositionTextView.text = formatMilliseconds(currentPosition.toLong())
                    }
                }
                delay(17)
            } while (songViewModel.currentSong.value?.isPlaying in listOf(
                    AudioState.PLAY,
                    AudioState.RESUME
                )
            )
        }
    }

}