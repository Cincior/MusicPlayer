package com.koszyk.musicplayer.domain.usecase

import com.google.firebase.firestore.FieldPath
import com.koszyk.musicplayer.data.DirectoriesRepository
import javax.inject.Inject

class UpdateFolderUseCase(
    private val directoriesRepository: DirectoriesRepository,
) {

    suspend fun execute(deviceId: String, folderPath: FieldPath, isChecked: Boolean) {
        directoriesRepository.updateFolderInDatabase(deviceId, folderPath, isChecked)
    }
}