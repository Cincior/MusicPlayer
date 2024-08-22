package com.example.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import pl.droidsonroids.gif.GifDrawable

class SongAdapter(private var items: ArrayList<Song>) :
    RecyclerView.Adapter<SongAdapter.ItemViewHolder>() {
    private var onActionListener: IonClickListener? = null
    private var defaultItems: ArrayList<Song> = ArrayList(items) // copy of all items which will be used for filtering e.g.

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val artistTextView: TextView = view.findViewById(R.id.songArtist)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
        val playingImage: ImageView = view.findViewById(R.id.right_image)
    }

    interface IonClickListener {
        fun onLongClick(position: Int, song: Song)
        fun onClick(holder: ItemViewHolder, song: Song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = items[position]
        holder.titleTextView.text = item.title
        holder.durationTextView.text = item.duration
        holder.artistTextView.text = item.artist

        val gif = holder.playingImage.drawable as GifDrawable

        when (item.isPlaying) {
            AudioState.NONE, AudioState.END -> {
                holder.playingImage.visibility = View.INVISIBLE
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.titleTextView.context,
                        R.color.white
                    )
                )
            }

            AudioState.PLAY -> {
                holder.playingImage.visibility = View.VISIBLE
                gif.start()
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.titleTextView.context,
                        R.color.skyBlue
                    )
                )
            }

            AudioState.PAUSE -> {
                holder.playingImage.visibility = View.VISIBLE
                gif.pause()
                holder.titleTextView.setTextColor(
                    ContextCompat.getColor(
                        holder.titleTextView.context,
                        R.color.white
                    )
                )
            }

        }

        holder.itemView.setOnClickListener {
            onActionListener?.onClick(holder, item)
        }
        holder.itemView.setOnLongClickListener {
            onActionListener?.onLongClick(holder.bindingAdapterPosition, item)
            true
        }
    }

    override fun getItemCount() = items.size

    fun filterSongs(query: String = "") {
        items = if (query.isNotEmpty()) {
            ArrayList(defaultItems.filter { it.title.startsWith(query, true) })
        } else {
            defaultItems
        }
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: IonClickListener?) {
        this.onActionListener = listener
    }

    fun updateAfterDeletion(id: Int, count: Int, delSongId: Long) {
        notifyItemRangeChanged(id, count)
        notifyItemRemoved(id)

        items.find {
            it.id == delSongId
        }.let {
            items.remove(it)
        }
        //update copy of items as well
        defaultItems.find {
            it.id == delSongId
        }.let {
            defaultItems.remove(it)
        }
    }

    fun insertNewItems(newSongs: ArrayList<Song>) {
        items = newSongs
        defaultItems = newSongs
    }
}




