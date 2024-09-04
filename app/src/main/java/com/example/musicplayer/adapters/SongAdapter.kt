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
import java.util.Locale
import kotlin.math.ceil

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
        holder.durationTextView.text = formatMilliseconds(item.duration)
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

            AudioState.PLAY, AudioState.RESUME -> {
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
            ArrayList(defaultItems.filter { it.title.contains(query, true) })
        } else {
            defaultItems
        }
        notifyDataSetChanged()
    }

    fun filterFavouriteSongs(favIds: Set<String>) {
        println(favIds)
        items = defaultItems.filter {
            it.id.toString() in favIds
        } as ArrayList<Song>

        println(items)
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
}




