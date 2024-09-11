package com.koszyk.musicplayer.model

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class FoldersWithSongsFinder() {

   fun getFoldersContainingAudioFiles(context: Context): List<String> {
        val audioFolders = mutableSetOf<String>()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataIndex)
                val folderPath = filePath.substring(0, filePath.lastIndexOf('/'))

                audioFolders.add(folderPath)
            }
        }

        return audioFolders.toList()
    }
}