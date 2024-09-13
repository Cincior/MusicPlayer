package com.koszyk.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.koszyk.musicplayer.model.FoldersWithSongsFinder
import com.koszyk.musicplayer.view.fragment.SettingsFragment.Companion.DEVICE_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel: ViewModel() {
    private val _dataState = MutableStateFlow<List<String>>(emptyList())
    val dataState: StateFlow<List<String>> get() = _dataState

    private val _checkboxStates = MutableStateFlow<List<Boolean>>(emptyList())
    val checkboxStates: StateFlow<List<Boolean>> get() = _checkboxStates

    private val _onStartCheckboxStates = MutableStateFlow<List<Boolean>>(emptyList())
    val onStartCheckboxStates: StateFlow<List<Boolean>> get() = _onStartCheckboxStates

    private val db = Firebase.firestore

    fun fetchDataFromFirebase(context: Context) {
        viewModelScope.launch {
            val docRef = db.collection("folders").document(DEVICE_ID)
            val folderFinder = FoldersWithSongsFinder()
            val foldersPaths = folderFinder.getFoldersContainingAudioFiles(context)

            val foldersFromStorage = foldersPaths.associateWith { false }.toMutableMap()

            docRef
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val documentData = document.data ?: emptyMap()

                        foldersFromStorage.forEach {
                            foldersFromStorage[it.key] = if (documentData[it.key] != null) documentData[it.key] as Boolean else false  //TO REFLECT THE ORDER FROM DATABASE
                        }

                        _dataState.value = foldersFromStorage.keys.toList()
                        _checkboxStates.value = foldersFromStorage.values.toList()
                        _onStartCheckboxStates.value = foldersFromStorage.values.toList()

                        if (documentData.size != foldersFromStorage.size) {
                            docRef.set(foldersFromStorage)
                                .addOnSuccessListener {
                                    println("Document successfully override with all folders!")
                                }
                                .addOnFailureListener { e ->
                                    println("Error creating document: $e")
                                }
                        }

                    } else {
                        docRef.set(foldersFromStorage)
                            .addOnSuccessListener {
                                println("Document successfully created with all folders!")
                            }
                            .addOnFailureListener { e ->
                                println("Error creating document: $e")
                            }
                    }
                }
        }
    }

    fun updateCheckboxState(index: Int, isChecked: Boolean) {
        val updatedStates = _checkboxStates.value.toMutableList()
        if (index in updatedStates.indices) {
            updatedStates[index] = isChecked
            _checkboxStates.value = updatedStates
        }
        updateDatabase(index, isChecked)
    }

    private fun updateDatabase(index: Int, isChecked: Boolean) {
        val docRef = db.collection("folders").document(DEVICE_ID)
        docRef.update(FieldPath.of(dataState.value[index]), isChecked)
            .addOnSuccessListener {
                println("Document successfully updated with new folders!")
            }
            .addOnFailureListener { e ->
                println("Error updating document: $e")
            }
    }
}