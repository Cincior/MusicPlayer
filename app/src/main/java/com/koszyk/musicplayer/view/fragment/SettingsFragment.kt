package com.koszyk.musicplayer.view.fragment

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.model.FoldersWithSongsFinder
import com.koszyk.musicplayer.viewmodel.FirebaseDirectoriesViewModel


class SettingsFragment : Fragment() {
    companion object {
        val DEVICE_ID: String =
            android.os.Build.MODEL; // MODEL NAME JUST FOR DATABASE PRESENTATION PURPOSES
    }

    private lateinit var existingData: Map<String, Any>
    private val db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: FirebaseDirectoriesViewModel by viewModels()
        viewModel.fetchDataFromFirebase(requireContext())
//        val folderFinder = FoldersWithSongsFinder()
//        val foldersPaths = folderFinder.getFoldersContainingAudioFiles(requireContext())
//
//        val folderNamesList = foldersPaths.map { it.substringAfterLast("/") }
//        val foldersToUpdate = folderNamesList.associateWith { false }
//
//        val docRef = db.collection("folders").document(DEVICE_ID)
//
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    // Get existing data from the document
//                    existingData = document.data ?: emptyMap()
//
//                    // Convert existing data to a map for easy comparison
//                    val existingFolders = existingData.keys.toSet()
//
//                    // Find new folders that need to be added
//                    val newFolders = foldersToUpdate.keys - existingFolders
//
//                    // Prepare the data to be updated
//                    val updates = mutableMapOf<String, Any>()
//
//                    for (folder in newFolders) {
//                        updates[folder] = false
//                    }
//
//                    // Update the document with new folders
//                    if (updates.isNotEmpty()) {
//                        docRef.update(updates)
//                            .addOnSuccessListener {
//                                println("Document successfully updated with new folders!")
//                            }
//                            .addOnFailureListener { e ->
//                                println("Error updating document: $e")
//                            }
//                    } else {
//                        println("No new folders to add.")
//                    }
//                } else {
//                    // Document does not exist, create it with all folders
//                    docRef.set(foldersToUpdate)
//                        .addOnSuccessListener {
//                            println("Document successfully created with all folders!")
//                        }
//                        .addOnFailureListener { e ->
//                            println("Error creating document: $e")
//                        }
//                }
//            }
//            .addOnFailureListener { e ->
//                println("Error getting document: $e")
//            }


        return ComposeView(requireContext()).apply {
            setContent {
                ShowFolders(viewModel)
            }
        }
    }

    @Composable
    private fun ShowFolders(viewModel: FirebaseDirectoriesViewModel) {

        val data = viewModel.dataState.collectAsState().value
        val checkboxStates = viewModel.checkboxStates.collectAsState().value.toMutableList()
        println("data: " + data)
        println("states: " + checkboxStates)

        if (data.isEmpty() || checkboxStates.isEmpty()) {
            Text(text = "LOADING", fontSize = 44.sp)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = checkboxStates.toList().toString(),
                    fontSize = 36.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(10.dp)
                )

                // Iterate through folders, using indices to bind the checkbox state to the correct item
                data.forEachIndexed { index, folderPath ->
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(colorResource(id = R.color.skyBlue))
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = folderPath.substringAfterLast("/"),
                                fontSize = 32.sp,
                                color = Color.White
                            )
                            Text(
                                text = folderPath,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Checkbox(
                            checked = checkboxStates[index],
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onCheckedChange = { checked ->
                                viewModel.updateCheckboxState(index, checked)
                            }
                        )
                    }
                }
            }
        }

    }

    @Composable
    private fun Showloading() {
        Column {
            Text(
                text = "XDDDD",
                fontSize = 44.sp
            )
        }
    }

}