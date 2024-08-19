package com.example.musicplayer.media

import android.content.Context
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel

class AudioPlayerManager(
    private val context: Context,
    private val songViewModel: SongViewModel,
    private val songAdapter: SongAdapter,
) {
    private var audioPlayer: AudioPlayer? = null

    fun playSong(song: Song) {
        audioPlayer?.stopSong()
        audioPlayer = AudioPlayer(context, song.uri)
        audioPlayer?.setOnCompletionListener {
            song.isPlaying = AudioState.END
            songViewModel.updatePlayingState(song)
            songAdapter.notifyDataSetChanged()

        }
        audioPlayer?.playSong()
        songViewModel.updatePlayingState(song)
        songAdapter.notifyDataSetChanged()
    }

    fun pauseSong(song: Song, holder: ItemViewHolder) {
        audioPlayer?.pauseSong()
        songViewModel.updatePlayingState(song)
        songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
    }

    fun resumeSong(song: Song, holder: ItemViewHolder) {
        audioPlayer?.resumeSong()
        songViewModel.updatePlayingState(song)
        songAdapter.notifyItemChanged(holder.bindingAdapterPosition)
    }

    fun pauseSongFragment(song: Song) {
        audioPlayer?.pauseSong()
        songViewModel.updatePlayingState(song)
        songAdapter.notifyDataSetChanged()
    }

    fun resumeSongFragment(song: Song) {
        audioPlayer?.resumeSong()
        songViewModel.updatePlayingState(song)
        songAdapter.notifyDataSetChanged()
    }


}