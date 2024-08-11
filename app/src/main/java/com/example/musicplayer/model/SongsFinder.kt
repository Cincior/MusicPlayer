package com.example.musicplayer.model

import android.app.Application
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.ceil

/**
 * It allows to find audio files in specific directory with help of MediaStore
 * @param application application context for getting contentResolver object
 */
class SongsFinder(private val application: Application)
{
    /**
     * Method for getting all audio files.
     * Looks for audio files in Download directory using MediaStore
     * @return list of founded songs in given directory
     */
    fun getSongsFromDownload(): ArrayList<Song>
    {

        val songs = ArrayList<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Download%") //DIRECTORY
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        application.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            while(cursor.moveToNext())
            {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val duration = cursor.getLong(durationCol)
                val artist = cursor.getString(artistCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                songs.add(Song(id, title, formatArtistName(artist), formatMilliseconds(duration), uri))

            }
        }
        return songs
    }

    /**
     * Function allows to change milliseconds to minutes and seconds
     * @param milliseconds song duration in milliseconds
     * @return duration in format M.SS (e.g. 2.43)
     */
    private fun formatMilliseconds(milliseconds: Long): String
    {
        val seconds = ceil(milliseconds / 1000.0)
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = seconds % 60

        return String.format(Locale.getDefault(), "%d:%02.0f", minutes, remainingSeconds)
    }

    private fun formatArtistName(artist: String): String
    {
        if(artist.contains("unknown"))
        {
            return "Artist unknown"
        }
        else
        {
            return artist
        }
    }


}