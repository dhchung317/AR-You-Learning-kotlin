package com.hyunki.aryoulearning2.ui.main.fragment.tutorial


import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.util.Objects

import javax.inject.Inject

//TODO refactor tutorial fragment
class TutorialFragment @Inject
constructor() : Fragment() {
    private lateinit var backButton: Button
    private lateinit var playVideoButton: Button
    private lateinit var startGameButton: Button
    private lateinit var listener: NavListener
    private lateinit var tutorialVideoView: VideoView

    private val isVideoViewPlaying: Boolean
        get() = tutorialVideoView.isPlaying

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            //            modelList = getArguments().getParcelableArrayList(MODEL_LIST);
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        viewClickListeners()
        playTutorial()
    }

    fun viewClickListeners() {
        startGameButton.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView.pause()
            }
            listener.moveToGameFragment()
        }
        backButton.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView.pause()
            }
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        playVideoButton.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView.pause()
                playVideoButton.setBackgroundResource(R.drawable.play_button_paused)
            } else {
                tutorialVideoView.start()
                playVideoButton.setBackgroundResource(R.drawable.play_button_playing)
            }
        }
    }

    private fun playTutorial() {
        val mediaController = MediaController(requireContext())
        tutorialVideoView.setMediaController(mediaController)
        val pathToTutorial =
            "android.resource://" + requireActivity().packageName + "/" + R.raw.ar_tutorial
        val tutorialUri = Uri.parse(pathToTutorial)
        tutorialVideoView.setVideoURI(tutorialUri)
    }

    private fun initializeViews(view: View) {
        tutorialVideoView = view.findViewById(R.id.tutorial_videoView)
        backButton = view.findViewById(R.id.tutorial_frag_back_to_hint_button)
        startGameButton = view.findViewById(R.id.tutorial_frag_start_game_button)
        playVideoButton = view.findViewById(R.id.tutorial_frag_play_video_button)
    }

    companion object {
        const val MODEL_LIST = "MODEL_LIST"
    }
}
