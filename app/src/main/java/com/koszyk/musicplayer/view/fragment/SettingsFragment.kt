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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import com.koszyk.musicplayer.R
import com.koszyk.musicplayer.model.Song
import com.koszyk.musicplayer.viewmodel.SettingsViewModel
import com.koszyk.musicplayer.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    companion object {
        val DEVICE_ID: String =
            android.os.Build.MODEL; // MODEL NAME JUST FOR DATABASE PRESENTATION PURPOSES
    }
    private val songViewModel: SongViewModel by activityViewModels()
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                viewModel = hiltViewModel()
                viewModel.fetchDataFromFirebase(requireContext())
                ShowFolders(viewModel)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (viewModel.onStartCheckboxStates.value != viewModel.checkboxStates.value) {
            songViewModel.changeIsCheckedState(true)
        }
    }

    @Composable
    private fun ShowFolders(viewModel: SettingsViewModel) {

        val data = viewModel.dataState.collectAsState().value
        val checkboxStates = viewModel.checkboxStates.collectAsState().value.toMutableList()
        val isLoading = viewModel.isLoading.collectAsState().value

        if (isLoading) {
            Showloading()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Available folders",
                        fontSize = 40.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier
                            .padding(10.dp)
                    )

                    // Iterate through folders, using indices to bind the checkbox state to the correct item
                    data.forEachIndexed { index, folderPath ->
                        Row(
                            modifier = Modifier
                                .padding(16.dp, 8.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(colorResource(id = R.color.skyBlue))
                                .fillMaxWidth()
                                .padding(12.dp, 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = folderPath.substringAfterLast("/"),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.W300,
                                    color = Color.White
                                )
                                Text(
                                    text = folderPath,
                                    fontWeight = FontWeight.W200,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (data.isEmpty()) "We couldn't find any folders. Download some audio on your device" else "Choose directories with audio that you want to see on the list",
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        modifier = Modifier
                            .padding(20.dp, 0.dp),
                        textAlign = TextAlign.Center
                    )
                }
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