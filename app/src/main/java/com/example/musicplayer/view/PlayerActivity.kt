package com.example.musicplayer.view

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val x = intent.getStringExtra("xd")
        findViewById<TextView>(R.id.tst).text = x.toString()
        val sArt = Uri.parse("content://media/external/audio/albumart")
        val uri = ContentUris.withAppendedId(sArt, x!!.toLong())
        Glide.with(this@PlayerActivity)
            .load(uri)
            .into(findViewById(R.id.albumThumbnail))

    }
}