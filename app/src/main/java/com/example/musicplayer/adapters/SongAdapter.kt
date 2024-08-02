package com.example.musicplayer.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import java.util.Locale

class SongAdapter(private val items: List<Song>, private val context: Context) : RecyclerView.Adapter<SongAdapter.ItemViewHolder>() {
    private lateinit var mediaPlayer: MediaPlayer
    private var currentlyPlayingPosition = -1
    lateinit var currentlyPlayingImage: ImageView
    lateinit var currentlyPlayingTitle: TextView

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
        val playingImage: ImageView = view.findViewById(R.id.right_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.durationTextView.text = formatMilliseconds(item.duration).toString()
        holder.itemView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(context, R.anim.song_clicked_animation)
            holder.itemView.startAnimation(animation)
            //notifyItemChanged(holder.adapterPosition)


            if (currentlyPlayingPosition != -1 && currentlyPlayingPosition != holder.adapterPosition) {
                mediaPlayer.stop()
                mediaPlayer.release()

                currentlyPlayingImage.visibility = View.INVISIBLE
                currentlyPlayingImage = holder.playingImage
                currentlyPlayingImage.visibility = View.VISIBLE


                currentlyPlayingTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                currentlyPlayingTitle = holder.titleTextView
                currentlyPlayingTitle.setTextColor(ContextCompat.getColor(context, R.color.skyBlue))


            }

            if (currentlyPlayingPosition == holder.adapterPosition) {
                mediaPlayer.stop()
                mediaPlayer.release()
                currentlyPlayingPosition = -1

                currentlyPlayingImage.visibility = View.INVISIBLE
                currentlyPlayingTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else
            {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(item.path)
                    prepare()
                    start()
                }
                currentlyPlayingPosition = holder.adapterPosition

                currentlyPlayingImage = holder.playingImage
                currentlyPlayingImage.visibility = View.VISIBLE

                currentlyPlayingTitle = holder.titleTextView
                currentlyPlayingTitle.setTextColor(ContextCompat.getColor(context, R.color.skyBlue))
            }
        }
    }

    override fun getItemCount() = items.size
}

fun formatMilliseconds(milliseconds: Long): String
{
    val seconds = milliseconds / 1000.0
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = seconds % 60

    return String.format(Locale.getDefault(), "%d.%02.0f", minutes, remainingSeconds)
}



