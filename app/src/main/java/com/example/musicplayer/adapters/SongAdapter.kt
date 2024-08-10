package com.example.musicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.media.AudioPlayer
import com.example.musicplayer.model.Song

class SongAdapter(private var items: ArrayList<Song>) : RecyclerView.Adapter<SongAdapter.ItemViewHolder>()
{
    private var onActionListener: IonClickListener? = null

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
        val playingImage: ImageView = view.findViewById(R.id.right_image)
    }

    interface IonClickListener
    {
        fun onLongClick(position: Int, item: Song)
        fun onClick(holder: ItemViewHolder, item: Song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int)
    {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.durationTextView.text = item.duration

        holder.itemView.setOnClickListener {
            onActionListener?.onClick(holder, item)
        }
        holder.itemView.setOnLongClickListener{
            onActionListener?.onLongClick(holder.adapterPosition, item)
            true
        }
    }

    override fun getItemCount() = items.size

    fun updateSongs(songs: ArrayList<Song>)
    {
        items = songs
    }

    fun setOnClickListener(listener: IonClickListener?)
    {
        this.onActionListener = listener
    }
}




