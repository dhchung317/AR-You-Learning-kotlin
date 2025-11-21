package com.hyunki.aryoulearning2.ui.main.fragment.replay

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.databinding.FragmentReplayBinding
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import javax.inject.Inject
import androidx.core.content.edit

//TODO refactor replay fragment
//TODO play again loads, but game does not start, debug arfragment on replay (going home works).
// probably needs fragment result listener setup
//TODO results not working at all
class ReplayFragment @Inject
constructor() : Fragment() {
    private var _binding: FragmentReplayBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: NavListener
    private lateinit var resultsButtonCard: CardView
    private lateinit var homeButtonCard: CardView
    private lateinit var playAgainButtonCard: CardView

    // TODO: implement pronunciation
    // private lateinit var textToSpeech: TextToSpeech
    // private lateinit var pronunciationUtil: PronunciationUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReplayBinding.inflate(inflater, container, false)
        return binding.root
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
        initializeViews()
        setClickListeners()
        //        textToSpeech = pronunciationUtil.getTTS(requireContext());
    }

    private fun initializeViews() {
        playAgainButtonCard = binding.cardViewPlayagain
        homeButtonCard = binding.cardViewHome
        resultsButtonCard = binding.cardViewResults
    }

    fun setClickListeners() {
        resultsButtonCard.setOnClickListener {
            //            pronunciationUtil.textToSpeechAnnouncer("Showing progress", textToSpeech);
            listener.moveToResultsFragment()
        }

        homeButtonCard.setOnClickListener {
            //            pronunciationUtil.textToSpeechAnnouncer("Lets go home", textToSpeech);
            listener.moveToListFragment()
        }

        playAgainButtonCard.setOnClickListener {
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

        val resultsFragment = parentFragmentManager.findFragmentByTag("results")
        if (resultsFragment?.isAdded == true) {
            parentFragmentManager.beginTransaction()
                .remove(resultsFragment)
                .commitAllowingStateLoss()
        }
    }

    override fun onDestroy() {
        _binding = null // prevent memory leak
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit { clear() }
        super.onDestroy()
    }
}
