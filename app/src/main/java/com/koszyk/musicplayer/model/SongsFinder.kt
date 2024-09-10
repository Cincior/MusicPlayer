package com.koszyk.musicplayer.model

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

/**
 * It allows to find audio files in specific directory with help of MediaStore
 * @param context application context for getting contentResolver object
 */
class SongsFinder(private val context: Context) {
    /**
     * Method for getting all audio files.
     * Looks for audio files in Download directory using MediaStore
     * @return list of founded songs in given directory
     */
    fun getSongsFromDownload(): ArrayList<Song> {

        val songs = ArrayList<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        //val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Download%") //DIRECTORY

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
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
            val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val duration = cursor.getLong(durationCol)
                val artist = cursor.getString(artistCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val albumId = cursor.getString(albumIdCol)
                songs.add(
                    Song(
                        id,
                        title,
                        formatArtistName(artist),
                        duration,
                        uri,
                        albumId,
                        AudioState.NONE
                    )
                )

            }
        }
        return songs
    }

    fun getSongsFromGivenDirectories(directories: List<String>): ArrayList<Song> {

        val songs = ArrayList<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        //val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArray: MutableList<String> = mutableListOf()
        directories.forEach {
            selectionArray.add(it)
        }
        val selectionArgs = selectionArray.toTypedArray() //DIRECTORY

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
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
            val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val duration = cursor.getLong(durationCol)
                val artist = cursor.getString(artistCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val albumId = cursor.getString(albumIdCol)
                songs.add(
                    Song(
                        id,
                        title,
                        formatArtistName(artist),
                        duration,
                        uri,
                        albumId,
                        AudioState.NONE
                    )
                )

            }
        }
        return songs
    }

    private fun formatArtistName(artist: String): String {
        return if (artist.contains("unknown")) {
            "Artist unknown"
        } else {
            artist
        }
    }


}