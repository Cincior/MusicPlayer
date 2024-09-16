package com.koszyk.musicplayer.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DirectoriesRepository (private val firebaseDataSource: FirebaseDataSource) {
    suspend fun getFoldersFromDatabase(deviceId: String): Map<String, Boolean>? {
        return firebaseDataSource.fetchFolders(deviceId)
    }

    suspend fun setFoldersInDatabase(deviceId: String, folders: Map<String, Boolean>) {
        firebaseDataSource.setFolders(deviceId, folders)
    }

    suspend fun updateFolderInDatabase(deviceId: String, folder: FieldPath, isChecked: Boolean) {
        firebaseDataSource.updateFolder(deviceId, folder, isChecked)
    }
}


class FirebaseDataSource (
    private val dataBase: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_NAME = "folders"
    }

    suspend fun fetchFolders(deviceId: String): Map<String, Boolean>? {
        val folders = dataBase.collection(COLLECTION_NAME).document(deviceId).get().await()
        return if (folders.exists()) {
            folders.data?.mapValues { it.value as Boolean }
        } else {
            null
        }
    }

    suspend fun setFolders(deviceId: String, folders: Map<String, Boolean>) {
        dataBase.collection(COLLECTION_NAME).document(deviceId)
            .set(folders).await()

    }

    suspend fun updateFolder(deviceId: String, folderPath: FieldPath, isChecked: Boolean) {
        val docRef = dataBase.collection(COLLECTION_NAME).document(deviceId)
        docRef.update(folderPath, isChecked).await()
    }
}