package com.koszyk.musicplayer.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.viewmodel.SettingsViewModel


class SettingsFragment : Fragment() {
    companion object {
        val DEVICE_ID: String =
            android.os.Build.MODEL; // MODEL NAME JUST FOR DATABASE PRESENTATION PURPOSES
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: SettingsViewModel by viewModels()
        viewModel.fetchDataFromFirebase(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                ShowFolders(viewModel)
            }
        }
    }

    @Composable
    private fun ShowFolders(viewModel: SettingsViewModel) {

        val data = viewModel.dataState.collectAsState().value
        val checkboxStates = viewModel.checkboxStates.collectAsState().value.toMutableList()

        if (data.isEmpty() || checkboxStates.isEmpty()) {
            Showloading()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Refresh song list to see changes!",
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

    }

    @Composable
    private fun Showloading() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(32.dp),
                color = Color.White,
                trackColor = colorResource(id = R.color.skyBlue)
            )
        }

    }

}