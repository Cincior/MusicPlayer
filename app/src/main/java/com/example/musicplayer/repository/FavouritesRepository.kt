package com.example.musicplayer.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favRepo")

class FavouritesRepository(context: Context) {
    companion object PreferencesKeys {
        val FAVORITE_SONGS = stringSetPreferencesKey("favorite_songs")
    }

    private val dataStore = context.dataStore

    suspend fun addSongToFavourites(songId: String) {
        dataStore.edit { preferences ->
            val favSongs = preferences[FAVORITE_SONGS]?.toMutableSet() ?: mutableSetOf()
            favSongs.add(songId)
            preferences[FAVORITE_SONGS] = favSongs
        }
    }

    suspend fun deleteAllFavourites() {
        dataStore.edit { preferences ->
            preferences[FAVORITE_SONGS] = emptySet()
        }
    }

    suspend fun deleteFavourite(songId: String) {
        dataStore.edit { preferences ->
            val favSongs = preferences[FAVORITE_SONGS]?.toMutableSet() ?: mutableSetOf()
            favSongs.removeIf {
                it == songId
            }
            preferences[FAVORITE_SONGS] = favSongs
        }
    }

    fun getFavouriteSongs(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[FAVORITE_SONGS]?.toMutableSet() ?: emptySet()
        }
    }

}