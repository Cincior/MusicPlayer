package com.example.musicplayer.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.model.Song

class SongAdapter(private val items: List<Song>, private val context: Context) : RecyclerView.Adapter<SongAdapter.ItemViewHolder>() {
    private lateinit var mediaPlayer: MediaPlayer
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.durationTextView.text = item.duration.toString()
        holder.itemView.setOnClickListener{

            mediaPlayer = MediaPlayer().apply {
                setDataSource(item.path)
                prepare()
                start()
            }
        }
    }

    override fun getItemCount() = items.size
}
