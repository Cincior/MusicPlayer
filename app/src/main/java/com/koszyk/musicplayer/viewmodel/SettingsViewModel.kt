package com.koszyk.musicplayer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.koszyk.musicplayer.data.DirectoriesRepository
import com.koszyk.musicplayer.data.FirebaseDataSource
import com.koszyk.musicplayer.domain.usecase.GetFoldersUseCase
import com.koszyk.musicplayer.domain.usecase.UpdateFolderUseCase
import com.koszyk.musicplayer.model.FoldersWithSongsFinder
import com.koszyk.musicplayer.view.fragment.SettingsFragment.Companion.DEVICE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getFoldersUseCase: GetFoldersUseCase,
    private val updateFolderUseCase: UpdateFolderUseCase
): ViewModel() {
    private val _dataState = MutableStateFlow<List<String>>(emptyList())
    val dataState: StateFlow<List<String>> get() = _dataState

    private val _checkboxStates = MutableStateFlow<List<Boolean>>(emptyList())
    val checkboxStates: StateFlow<List<Boolean>> get() = _checkboxStates

    private val _onStartCheckboxStates = MutableStateFlow<List<Boolean>>(emptyList())
    val onStartCheckboxStates: StateFlow<List<Boolean>> get() = _onStartCheckboxStates

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun fetchDataFromFirebase(context: Context) {
        _isLoading.value = true
        viewModelScope.launch {
            val folderFinder = FoldersWithSongsFinder()
            val foldersPaths = folderFinder.getFoldersContainingAudioFiles(context)

            val foldersFromStorage = foldersPaths.associateWith { false }.toMutableMap()

            viewModelScope.launch {
                try {
                    val foldersFromFirebase = getFoldersUseCase.execute(DEVICE_ID, foldersFromStorage)

                    _dataState.value = foldersFromFirebase.keys.toList()
                    _checkboxStates.value = foldersFromFirebase.values.toList()
                    _onStartCheckboxStates.value = foldersFromFirebase.values.toList()
                } finally {
                    _isLoading.value = false
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
        viewModelScope.launch {
            updateFolderUseCase.execute(DEVICE_ID, FieldPath.of(dataState.value[index]), isChecked)
        }
    }
}