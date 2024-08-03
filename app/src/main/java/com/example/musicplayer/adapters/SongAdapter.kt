package com.example.musicplayer.adapters

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import java.io.File
import java.util.Locale
import kotlin.math.ceil

class SongAdapter(private val items: ArrayList<Song>, private val context: Context) : RecyclerView.Adapter<SongAdapter.ItemViewHolder>()
{
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingPosition = -1 // stores position of song that is currently playing
    private var currentlyMarked = -1
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
        val playingImage: ImageView = view.findViewById(R.id.right_image)
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
        holder.durationTextView.text = formatMilliseconds(item.duration)

        checkClicedElement(currentlyPlayingPosition, holder, position)


        holder.itemView.setOnClickListener {
            songClicked(holder, item)
        }
        holder.itemView.setOnLongClickListener{
            songLongClicked(holder, item)
            Toast.makeText(context, "dont hold me!", Toast.LENGTH_SHORT).show()

            true
        }
    }

    private fun checkClicedElement(
        currentlyPlayingPosition: Int,
        holder: ItemViewHolder,
        position: Int
    ) {
        if(currentlyPlayingPosition == position)
        {
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.skyBlue))
            holder.playingImage.visibility = View.VISIBLE
        }
        else
        {
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.playingImage.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = items.size

    /**
    * Method responsible for playing or stopping music, showing animation and changing currentlyPlayingPosition variable
    * @param holder contains whole song UI elements
    * @param item clicked Song object
    */
    private fun songClicked(holder: ItemViewHolder, item: Song)
    {
        val animation = AnimationUtils.loadAnimation(context, R.anim.song_clicked_animation)
        holder.itemView.startAnimation(animation)

        if (currentlyPlayingPosition != -1)
        {
            notifyItemChanged(currentlyPlayingPosition)
        }
        if(currentlyPlayingPosition == holder.adapterPosition)
        {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            currentlyPlayingPosition = -1
        }
        else
        {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(item.path)
                prepare()
                start()
            }
            currentlyPlayingPosition = holder.adapterPosition
        }
        notifyItemChanged(holder.adapterPosition)

    }

    private fun songLongClicked(holder: ItemViewHolder, item: Song) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle(item.title)
            .setItems(arrayOf("Delete", "Item Two")) { dialog, which ->
                if(which == 0)
                {
                    Toast.makeText(context, "NOT IMPLEMENTED YET", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(context, "clicked 2", Toast.LENGTH_SHORT).show()
                }
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}

/**
 * Function allows to change milliseconds to minutes and seconds
 * @param milliseconds song duration in milliseconds
 * @return duration in format M.SS (e.g. 2.43)
 */
fun formatMilliseconds(milliseconds: Long): String
{
    val seconds = ceil(milliseconds / 1000.0)
    val minutes = (seconds / 60).toInt()
    val remainingSeconds = seconds % 60

    return String.format(Locale.getDefault(), "%d.%02.0f", minutes, remainingSeconds)
}



