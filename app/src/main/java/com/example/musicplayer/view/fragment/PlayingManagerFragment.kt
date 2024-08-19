package com.example.musicplayer.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioState
import com.example.musicplayer.model.Song
import com.example.musicplayer.view.PlayerActivity
import com.example.musicplayer.viewmodel.SongViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "audioState"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayingManagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayingManagerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var audioState: String? = null

    private lateinit var songViewModel: SongViewModel
    private lateinit var textViewTitle: TextView
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var titleSection: LinearLayout
    private var actionListener: IonActionListener? = null

    interface IonActionListener {
        fun onButtonPlayPauseClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            audioState = it.getString(ARG_PARAM2)
        }
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val playingManagerView =
            inflater.inflate(R.layout.fragment_playing_manager, container, false)

        songViewModel.items.observe(viewLifecycleOwner) { songs ->
            changeFragmentLayout(songs)
        }
        textViewTitle = playingManagerView.findViewById(R.id.songTitleFragment)

        buttonPlayPause = playingManagerView.findViewById(R.id.btnPlayPauseFragment)
        buttonPlayPause.setOnClickListener {
            actionListener?.onButtonPlayPauseClicked()
        }

        titleSection = playingManagerView.findViewById(R.id.titleSectionFragment)
        titleSection.setOnClickListener {
            val playerActivityIntent = Intent(requireContext(), PlayerActivity::class.java)
            startActivity(playerActivityIntent)
        }

        // Inflate the layout for this fragment
        return playingManagerView
    }

    private fun changeFragmentLayout(songs: ArrayList<Song>?) {
        songs?.find {
            it.isPlaying == AudioState.PLAY || it.isPlaying == AudioState.PAUSE
        }.let {
            textViewTitle.text = it?.title
            when (it?.isPlaying) {
                AudioState.PLAY -> {
                    buttonPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause_btn
                        )
                    )
                }

                AudioState.PAUSE -> {
                    buttonPlayPause.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_play_btn
                        )
                    )
                }

                else -> TODO()
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayingManagerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayingManagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun changePlayPauseButtonIcon(audioState: AudioState) {
        when (audioState) {
            AudioState.PLAY -> {
                buttonPlayPause.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_pause_btn
                    )
                )
            }

            AudioState.PAUSE -> {
                buttonPlayPause.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play_btn
                    )
                )
            }

            else -> println("ERR!")
        }


    }

    fun setActionListener(listener: IonActionListener) {
        this.actionListener = listener
    }
}