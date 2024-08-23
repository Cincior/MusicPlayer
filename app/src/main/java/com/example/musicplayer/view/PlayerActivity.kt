package com.example.musicplayer.view

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.SongViewModel
import com.example.musicplayer.viewmodel.ViewModelSingleton

class PlayerActivity : AppCompatActivity() {
    private val songViewModel: SongViewModel by lazy {
        ViewModelSingleton.getSharedViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        findViewById<TextView>(R.id.head).text = songViewModel.repeat.value.toString()

        val buttonRepeat = findViewById<ImageButton>(R.id.btnRepeat)
        buttonRepeat.setOnClickListener{
            if (songViewModel.repeat.value == true) {
                songViewModel.setRepetition(false)
                buttonRepeat.setColorFilter(getColor(R.color.skyBlue))
            } else {
                songViewModel.setRepetition(true)
                buttonRepeat.setColorFilter(getColor(R.color.darkBlue))
            }
            findViewById<TextView>(R.id.head).text = songViewModel.repeat.value.toString()
        }


//        val x = intent.getStringExtra("xd")
//        findViewById<TextView>(R.id.tst).text = x.toString()
//        val sArt = Uri.parse("content://media/external/audio/albumart")
//        val uri = ContentUris.withAppendedId(sArt, x!!.toLong())
//        Glide.with(this@PlayerActivity)
//            .load(uri)
//            .into(findViewById(R.id.albumThumbnail))

    }
}