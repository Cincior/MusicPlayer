package com.example.musicplayer.view.mainActivityPackage

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.adapters.SongAdapter.ItemViewHolder


fun changeAppearance(previousHolder: ItemViewHolder, currentHolder: ItemViewHolder, songAdapter: SongAdapter, context: Context)
{
    currentHolder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.skyBlue))
    currentHolder.playingImage.visibility = View.VISIBLE
    songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)

    previousHolder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    previousHolder.playingImage.visibility = View.INVISIBLE
    songAdapter.notifyItemChanged(previousHolder.bindingAdapterPosition)

}
fun changeAppearance(currentHolder: ItemViewHolder, songAdapter: SongAdapter, context: Context)
{
    currentHolder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.skyBlue))
    currentHolder.playingImage.visibility = View.VISIBLE
    songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)
}

fun resetAppearance(currentHolder: ItemViewHolder, songAdapter: SongAdapter, context: Context)
{
    currentHolder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
    currentHolder.playingImage.visibility = View.INVISIBLE
    songAdapter.notifyItemChanged(currentHolder.bindingAdapterPosition)
}