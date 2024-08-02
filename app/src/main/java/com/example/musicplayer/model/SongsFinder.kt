package com.example.musicplayer.model

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class SongsFinder()
{
    /**
     * Async method - getting all audio files takes a while
     * Looks for audio files in Download directory
     * @return list of founded songs in given directory
     */
    suspend fun getSongsFromDownload(): ArrayList<Song> = withContext(Dispatchers.IO)
    {
        var id = 0
        val songList = ArrayList<Song>()
        val folderPath = "/storage/emulated/0/Download/"
        val getFolder = File(folderPath) // THIS PATH CAN CHANGE
        if (getFolder.exists() && getFolder.isDirectory)
        {
            val files = getFolder.listFiles()
            if (files != null)
            {
                for (file in files)
                {
                    if (file.isFile && ( file.extension.equals("mp3", ignoreCase = true) || file.extension.equals("wav", ignoreCase = true)))
                    {
                        songList.add(Song(id, file.name, (getSongDuration(folderPath + file.name)), folderPath + file.name))
                    }
                    id++
                }
            }
        }
        return@withContext songList
    }

    /**
     * Getting song duration with MediaMetadataRetriever
     * @param filePath path to audio file
     * @return duration in milliseconds or 0 if file doesn't exist
     */
    private fun getSongDuration(filePath: String): Long // TIME IN MILLISECONDS
    {
        val file = File(filePath)
        if (!file.exists())
        {
            return 0L
        }

        val retriever = MediaMetadataRetriever()
        return try
        {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        }
        catch (e: IllegalArgumentException)
        {
            0L
        }
        finally
        {
            retriever.release()
        }
    }

}