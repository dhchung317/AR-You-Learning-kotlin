package com.hyunki.aryoulearning2.ui.main.fragment.replay

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject

//TODO refactor replay fragment
class ReplayFragment @Inject
constructor() : Fragment() {
    private lateinit var listener: NavListener

    private lateinit var resultsButtonCard: CardView
    private lateinit var homeButtonCard: CardView
    private lateinit var playAgainButtonCard: CardView

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var pronunciationUtil: PronunciationUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_replay, container, false)
    }

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
        //        AndroidSupportInjection.inject(this);
        //        pronunciationUtil = new PronunciationUtil();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        viewClickListeners()
        //        textToSpeech = pronunciationUtil.getTTS(requireContext());
    }

    private fun initializeViews(view: View) {
        playAgainButtonCard = view.findViewById(R.id.cardView_playagain)
        homeButtonCard = view.findViewById(R.id.cardView_home)
        resultsButtonCard = view.findViewById(R.id.cardView_results)
    }

    fun viewClickListeners() {
        resultsButtonCard.setOnClickListener { v ->
            //            pronunciationUtil.textToSpeechAnnouncer("Showing progress", textToSpeech);
            listener.moveToResultsFragment()
        }

        homeButtonCard.setOnClickListener { v ->
            //            pronunciationUtil.textToSpeechAnnouncer("Lets go home", textToSpeech);
            listener.moveToListFragment()
        }

        playAgainButtonCard.setOnClickListener { v ->
            //            pronunciationUtil.textToSpeechAnnouncer("Lets play again!", textToSpeech);
            listener.moveToGameFragment()
        }
    }

    override fun onDetach() {
        super.onDetach()
        //        textToSpeech.shutdown();
        //        pronunciationUtil = null;
        //        listener = null;
    }

    override fun onResume() {
        super.onResume()
        if (fragmentManager?.findFragmentByTag("result_fragment") != null) {
            fragmentManager?.beginTransaction()?.remove(requireFragmentManager().findFragmentByTag("result_fragment")!!)?.commit()
        }
        //        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }
}
