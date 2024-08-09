package com.example.musicplayer.adapters

import android.content.Context
import android.provider.MediaStore
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
import com.example.musicplayer.media.AudioPlayer
import com.example.musicplayer.model.Song

class SongAdapter(
    private var items: ArrayList<Song>,
    private val context: Context,
    private val intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>)
    : RecyclerView.Adapter<SongAdapter.ItemViewHolder>()
{
    private var audioPlayer: AudioPlayer? = null
    private var currentlyPlayingPosition = -1 // stores position of song that is currently playing
    private var onClickListener: IonClickListener? = null

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val titleTextView: TextView = view.findViewById(R.id.songTitle)
        val durationTextView: TextView = view.findViewById(R.id.songDuration)
        val playingImage: ImageView = view.findViewById(R.id.right_image)
    }

    interface IonClickListener
    {
        fun onLongClick(position: Int, item: Song)
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

        checkClicedElement(currentlyPlayingPosition, holder, position)

        holder.itemView.setOnClickListener {
            songClicked(holder, item)
        }
        holder.itemView.setOnLongClickListener{
            onClickListener?.onLongClick(holder.adapterPosition, item)
            true
        }
    }

    private fun checkClicedElement(
        currentlyPlayingPosition: Int,
        holder: ItemViewHolder,
        position: Int
    )
    {
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
            audioPlayer?.stopSong()
        }
        if(currentlyPlayingPosition == holder.adapterPosition)
        {
            audioPlayer?.stopSong()
            currentlyPlayingPosition = -1
        }
        else
        {
            audioPlayer = AudioPlayer(context, item.uri).apply {
                playSong()
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
                    deleteSong(item, holder.adapterPosition)
                }
                else
                {
                    Toast.makeText(context, "clicked 2", Toast.LENGTH_SHORT).show()
                }
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteSong(item: Song, adapterPosition: Int)
    {
        val deleteRequest = MediaStore.createDeleteRequest(context.contentResolver, listOf(item.uri))
        intentSenderLauncher.launch(IntentSenderRequest.Builder(deleteRequest).build())
        notifyItemRemoved(adapterPosition)
    }


    fun updateSongs(id: Int) {
        items.removeAt(id)
        //notifyDataSetChanged() // Powiadamia adapter, że dane uległy zmianie
    }

    fun updateSongsTest(songs: ArrayList<Song>) {
        items = songs
        //notifyDataSetChanged() // Powiadamia adapter, że dane uległy zmianie
    }


    fun setOnClickListener(listener: IonClickListener?) {
        this.onClickListener = listener
    }
}




