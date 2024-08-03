package com.example.musicplayer.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.musicplayer.R
import com.example.musicplayer.adapters.SongAdapter
import com.example.musicplayer.viewmodel.SongViewModel


class MainActivity : AppCompatActivity()
{
    companion object
    {
        var permissionGranted = false
    }
    private val requestCodeReadMemory = 0
    private val songViewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MusicPlayer)
        setContentView(R.layout.activity_main)

        // MAIN LOGIC
        getPermission()
        if(!permissionGranted)
        {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.songList)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        songViewModel.items.observe(this, Observer { items -> recyclerView.adapter = SongAdapter(items, this) })
    }

    /**
     * Asks user for needed permission (to get audio files from memory) in way that depends on currently using system
     * assign permissionGranted = true if user gave permission
     */
    private fun getPermission()
    {
        if(getSystemVersion() >= 33)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    requestCodeReadMemory)
            }
            else
            {
                permissionGranted = true
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    requestCodeReadMemory)
            }
            else
            {
                permissionGranted = true
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeReadMemory) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                permissionGranted = true
            }
            else
            {
                Toast.makeText(this, "Odmowa uprawnien", Toast.LENGTH_SHORT).show()
                permissionGranted = false
            }
        }
    }

    /**
     * @return version of android system
     */
    private fun getSystemVersion(): Int
    {
        return android.os.Build.VERSION.SDK_INT
    }

}