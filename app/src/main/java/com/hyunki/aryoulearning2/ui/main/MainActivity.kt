package com.hyunki.aryoulearning2.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.ui.main.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.hint.HintFragment
import com.hyunki.aryoulearning2.ui.main.list.ListFragment
import com.hyunki.aryoulearning2.ui.main.replay.ReplayFragment
import com.hyunki.aryoulearning2.ui.main.results.ResultsFragment
import com.hyunki.aryoulearning2.ui.main.tutorial.TutorialFragment
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
    lateinit var listFragment: ListFragment

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
        viewModel = ViewModelProviders.of(this, providerFactory).get(MainViewModel::class.java)

        progressBar = findViewById(R.id.progress_bar)
        Log.d(TAG, "onCreate")
//        if (prefs.contains(NETWORK_CALL_COMPLETED)) {
//            Log.d(TAG, "onCreate: " + prefs.contains(NETWORK_CALL_COMPLETED))
//            moveToListFragment()
//        } else {
            viewModel.loadModelResponses()
            viewModel.modelResponsesData.observe(this, Observer<MainState> { this.renderModelResponses(it) })
//        }
    }

    private fun renderModelResponses(state: MainState) {
        if (state === MainState.Loading) {
            showProgressBar(true)
        } else if (state === MainState.Error) {
            showProgressBar(false)
        } else if (state.javaClass == MainState.Success.OnModelResponsesLoaded::class.java) {
            prefs.edit().putString(NETWORK_CALL_COMPLETED, "success").apply()
            moveToListFragment()
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
                .replace(R.id.fragment_container, listFragment)
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

    override fun setCategoryFromListFragment(category: Category) {
        viewModel.setCurrentCategory(category)
    }

    override fun setWordHistoryFromGameFragment(wordHistory: List<CurrentWord>) {
        viewModel.wordHistory = wordHistory
    }

    companion object {
        const val TAG = "MainActivity"
        const val NETWORK_CALL_COMPLETED = "network_call_completed"
    }
}
