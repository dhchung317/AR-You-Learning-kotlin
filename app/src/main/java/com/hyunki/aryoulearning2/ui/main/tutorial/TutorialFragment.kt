package com.hyunki.aryoulearning2.ui.main.tutorial


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

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import java.util.Objects

import javax.inject.Inject

class TutorialFragment @Inject
constructor() : Fragment() {
    private var backButton: Button? = null
    private var playVideoButton: Button? = null
    private var startGameButton: Button? = null
    private var listener: NavListener? = null
    private var tutorialVideoView: VideoView? = null

    private val isVideoViewPlaying: Boolean
        get() = tutorialVideoView!!.isPlaying

    override fun onAttach(context: Context) {
        (activity!!.application as BaseApplication).appComponent.inject(this)
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tutorial_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        viewClickListeners()
        playTutorial()
    }

    fun viewClickListeners() {
        startGameButton!!.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView!!.pause()
            }
            listener!!.moveToGameFragment()
        }
        backButton!!.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView!!.pause()
            }
            activity!!.onBackPressed()
        }
        playVideoButton!!.setOnClickListener { v ->
            if (isVideoViewPlaying) {
                tutorialVideoView!!.pause()
                playVideoButton!!.setBackgroundResource(R.drawable.play_button_paused)
            } else {
                tutorialVideoView!!.start()
                playVideoButton!!.setBackgroundResource(R.drawable.play_button_playing)
            }
        }
    }

    private fun playTutorial() {
        val mediaController = MediaController(requireContext())
        tutorialVideoView!!.setMediaController(mediaController)
        val pathToTutorial = "android.resource://" + Objects.requireNonNull<FragmentActivity>(activity).getPackageName() + "/" + R.raw.ar_tutorial
        val tutorialUri = Uri.parse(pathToTutorial)
        tutorialVideoView!!.setVideoURI(tutorialUri)
    }

    private fun initializeViews(view: View) {
        tutorialVideoView = view.findViewById(R.id.tutorial_videoView)
        backButton = view.findViewById(R.id.tutorial_frag_back_to_hint_button)
        startGameButton = view.findViewById(R.id.tutorial_frag_start_game_button)
        playVideoButton = view.findViewById(R.id.tutorial_frag_play_video_button)
    }

    companion object {
        val MODEL_LIST = "MODEL_LIST"
    }
}
