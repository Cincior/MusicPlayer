package com.example.musicplayer.view.fragment

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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.media.MusicPlayerService
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.view.MainActivity
import com.example.musicplayer.viewmodel.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

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

        lifecycleScope.launch {
            loadThumbnail(songThumbnail)
        }

        titleTextView.text = songViewModel.currentSong.value?.title ?: "Unknown title"

        duration.text = formatMilliseconds(songViewModel.currentSong.value?.duration ?: 0)

        seekBar.max = songViewModel.currentSong.value?.duration?.toInt() ?: 0

        buttonRepeat.setOnClickListener {
            songViewModel.toggleRepetition()
        }

        songViewModel.repeat.observe(viewLifecycleOwner) {
            toggleRepetition()
        }

        songViewModel.currentSong.observe(viewLifecycleOwner) { cSong ->
            println("observer: " + cSong.isPlaying)
            if (cSong.isPlaying in listOf(AudioState.PLAY, AudioState.RESUME, AudioState.PAUSE)) {
                lifecycleScope.launch {
                    updateSeekbar()
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(s: SeekBar?) {

            }

            override fun onStopTrackingTouch(s: SeekBar) {
                if (songViewModel.currentSong.value?.isPlaying in listOf(
                        AudioState.END,
                        AudioState.END
                    )
                ) {
                    songViewModel.changeCurrentSongState()
                }
                musicService?.audioPlayer?.setCurrentPosition(s.progress.toLong())
            }
        })

        buttonPlayPause.setOnClickListener {
            songViewModel.changeCurrentSongState()
            //cosik cza wymyslic zamaist isSongPlaying to jakis observer
        }

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