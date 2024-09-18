package com.koszyk.musicplayer.view.fragment

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class TopListFragment : Fragment() {
    companion object {
        private const val CLIENT_ID = "d8c1e72bd55b442bab4cb78db4c0c978"
    }

    private lateinit var clientSecret: String
    private var accessToken by mutableStateOf("")
    private var top10Tracks by mutableStateOf<List<TrackItem>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        clientSecret = readJsonFileFromAssets("client-secret-spotify.json")
        clientSecret = extractClientSecret(clientSecret)

        lifecycleScope.launch {
            try {
                accessToken = getSpotifyAccessToken()
                top10Tracks = fetchTop10Tracks()
            } catch (e: Exception) {
                // Handle the error if needed
                println("Error: ${e.message}")
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                ShowList(top10Tracks)
            }
        }
    }

    @Composable
    private fun ShowList(tracks: List<TrackItem>) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top 10 Songs",
                fontSize = 30.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            tracks.forEachIndexed { index, trackItem ->
                val track = trackItem.track
                val artists = track.artists.joinToString(", ") { it.name }
                Text(
                    text = "${index + 1}. ${track.name} | $artists",
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Left
                )
            }
        }
    }

    private fun readJsonFileFromAssets(fileName: String): String {
        val assetManager = requireContext().assets
        val inputStream = assetManager.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }

    private fun extractClientSecret(jsonString: String): String {
        val regex = """"client-secret"\s*:\s*"(.+?)"""".toRegex()
        val matchResult = regex.find(jsonString)
        return matchResult?.groups?.get(1)?.value ?: "Client Secret Not Found"
    }

    private suspend fun getSpotifyAccessToken(): String {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(requestBody)
            .addHeader("Authorization", Credentials.basic(CLIENT_ID, clientSecret))
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val json = Gson().fromJson(responseBody, TokenResponse::class.java)
            json.access_token
        }
    }

    private suspend fun fetchTop10Tracks(): List<TrackItem> {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/37i9dQZEVXbMDoHDwVN2tF/tracks?limit=10")
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val topTracks = Gson().fromJson(responseBody, SpotifyTrackResponse::class.java)
            topTracks.items
        }
    }
}

data class TokenResponse(val access_token: String)

data class SpotifyTrackResponse(val items: List<TrackItem>)

data class TrackItem(val track: Track)

data class Track(val name: String, val artists: List<Artist>)

data class Artist(val name: String)
