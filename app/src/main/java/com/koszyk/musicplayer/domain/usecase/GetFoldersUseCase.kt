package com.koszyk.musicplayer.domain.usecase

import com.koszyk.musicplayer.data.DirectoriesRepository
import javax.inject.Inject

class GetFoldersUseCase(
    private val directoriesRepository: DirectoriesRepository,
) {

    suspend fun execute(
        deviceId: String,
        foldersFromStorage: MutableMap<String, Boolean>,
    ): Map<String, Boolean> {
        val foldersFromFirebase = directoriesRepository.getFoldersFromDatabase(deviceId)

        if (foldersFromFirebase != null) {
            foldersFromStorage.forEach {
                foldersFromStorage[it.key] = foldersFromFirebase[it.key] ?: false
            }

            // Insert currently existing folders if there are differences between storage and firebase
            // TODO separate this logic
            if (foldersFromFirebase.size != foldersFromStorage.size) {
                directoriesRepository.setFoldersInDatabase(deviceId, foldersFromStorage)
            }
        } else {
            directoriesRepository.setFoldersInDatabase(deviceId, foldersFromStorage)
        }
        println("getFoldersUseCase Works!")

        return foldersFromStorage
    }
}