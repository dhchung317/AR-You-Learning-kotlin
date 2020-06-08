package com.hyunki.aryoulearning2.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.hint.HintFragment
import com.hyunki.aryoulearning2.ui.main.fragment.category.CategoryFragment
import com.hyunki.aryoulearning2.ui.main.fragment.replay.ReplayFragment
import com.hyunki.aryoulearning2.ui.main.fragment.results.ResultsFragment
import com.hyunki.aryoulearning2.ui.main.fragment.tutorial.TutorialFragment
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavListener {
    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var prefs: SharedPreferences

    @Inject
    lateinit var pronunciationUtil: PronunciationUtil
    @Inject
    lateinit var arHostFragment: ArHostFragment
    @Inject
    lateinit var categoryFragment: CategoryFragment
    @Inject
    lateinit var hintFragment: HintFragment
    @Inject
    lateinit var replayFragment: ReplayFragment
    @Inject
    lateinit var resultsFragment: ResultsFragment
    @Inject
    lateinit var tutorialFragment: TutorialFragment
    @JvmField
    @Inject
    var resId: Int = 0
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val decorView = window.decorView

        if (hasFocus) {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(resId)
        (application as BaseApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        viewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)

        progressBar = findViewById(R.id.progress_bar)
        Log.d(TAG, "onCreate")
//        if (prefs.contains(NETWORK_CALL_COMPLETED)) {
//            Log.d(TAG, "onCreate: " + prefs.contains(NETWORK_CALL_COMPLETED))
//            moveToListFragment()
//        } else {
            viewModel.loadModelResponses()
            viewModel.getModelResponsesData().observe(this, Observer<MainState> { this.renderModelResponses(it) })
//        }
    }

    private fun renderModelResponses(state: MainState) {
        when (state) {
            is MainState.Loading -> showProgressBar(true)
            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnModelResponsesLoaded -> {
                prefs.edit().putString(NETWORK_CALL_COMPLETED, "success").apply()
                moveToListFragment()
            }
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun moveToListFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, categoryFragment)
                .commit()
    }

    override fun moveToGameFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, arHostFragment)
                .commit()
    }

    override fun moveToResultsFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, resultsFragment)
                .commit()
    }

    override fun moveToHintFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, hintFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun moveToReplayFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, replayFragment)
                .commit()
    }

    override fun moveToTutorialFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, tutorialFragment)
                .addToBackStack(null)
                .commit()
    }
    override fun saveWordHistoryFromGameFragment(wordHistory: List<CurrentWord>) {
        viewModel.setWordHistory(wordHistory)
    }

    companion object {
        const val TAG = "MainActivity"
        const val NETWORK_CALL_COMPLETED = "network_call_completed"
    }
}
