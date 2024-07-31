package com.example.musicplayer.model

import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File


class SongsFinder()
{

    fun getSongs(): ArrayList<Song>
    {
        val songList = arrayListOf(
            Song(1, "test", 2.00, "/storage/emulated/0/Download/SHINY FINALLLLLL WAV.wav")
        )
        return songList
    }

    fun getSongsFromDwonload(): ArrayList<Song>
    {
        var id = 0
        val songList = ArrayList<Song>()
        val getFolder = File("/storage/emulated/0/Download") // THIS PATH CAN CHANGE
        if (getFolder.exists() && getFolder.isDirectory)
        {
            val files = getFolder.listFiles()
            if (files != null)
            {
                for (file in files)
                {
                    if (file.isFile && ( file.extension.equals("mp3", ignoreCase = true) || file.extension.equals("wav", ignoreCase = true)))
                    {
                        songList.add(Song(id, file.name, (getSongDuration("/storage/emulated/0/Download/" + file.name)).toDouble() / 60000, "/storage/emulated/0/Download/" + file.name))
                    }
                    id++
                }
            }
        }
        Log.i("xd", songList.toString())
        return songList
    }

    private fun getSongDuration(filePath: String): Long // TIME IN MILLISECONDS
    {
        val file = File(filePath)
        if (!file.exists()) {
            println("File does not exist: $filePath")
            return 0L
        }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: IllegalArgumentException) {
            println("Failed to retrieve duration for file: $filePath")
            0L
        } finally {
            retriever.release()
        }
    }
//    private fun getDuration(file: File): String {
//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(file.absolutePath)
//        val durationStr =
//            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        return Utils.formateMilliSeccond(durationStr!!.toLong())
//    }
//    fun dur(path: String): Int
//    {
//        val mp = MediaPlayer.create(context, Uri.parse(path))
//        val duration = mp.duration
//        return duration
//    }
}