package com.example.musicplayer.view.mainActivityHelpers

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicplayer.view.MainActivity
import com.example.musicplayer.view.MainActivity.Companion.permissionGranted


const val requestCodeReadMemory = 0
/**
 * Asks user for needed permission (to get audio files from memory) in way that depends on currently using system
 * assign permissionGranted = true if user gave permission
 */
fun getPermission(activity: MainActivity)
{
    if(getSystemVersion() >= 33)
    {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                requestCodeReadMemory
            )
        }
        else
        {
            permissionGranted = true
        }
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            0
        )
    }
    else
    {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCodeReadMemory
            )
        }
        else
        {
            permissionGranted = true
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