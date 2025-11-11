package com.hyunki.aryoulearning2.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ProgressBar

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.databinding.ActivityMainBinding
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
import androidx.core.content.edit

class MainActivity : AppCompatActivity(), NavListener {
    private lateinit var binding: ActivityMainBinding
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
        if (hasFocus) {
            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Hide system bars with transient behavior
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            window.insetsController?.show(WindowInsets.Type.systemBars())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(resId)
        (application as BaseApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        viewModel = ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)

        if (prefs.contains(NETWORK_CALL_COMPLETED)) {
            moveToListFragment()
        } else {
            viewModel.loadModelResponses()
            viewModel.getModelResponsesData()
                .observe(this, Observer<MainState> { this.renderModelResponses(it) })
        }
    }

    private fun renderModelResponses(state: MainState) {
        when (state) {
            is MainState.Loading -> showProgressBar(true)
            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnModelResponsesLoaded -> {
                prefs.edit { putString(NETWORK_CALL_COMPLETED, "success") }
                moveToListFragment()
            }

            else -> showProgressBar(false)
        }
    }

    public fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun changeFragment(f: Fragment, backstack: Boolean = false, tag: String?) {
        val mgr = supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentContainer.id, f, tag);
        if (backstack) {
            mgr.addToBackStack(null)
        }
        mgr.commit()
    }

    override fun moveToListFragment() {
        changeFragment(categoryFragment, false, null)
    }

    override fun moveToGameFragment() {
        changeFragment(arHostFragment, false, null)
    }

    override fun moveToResultsFragment() {
        changeFragment(resultsFragment, false, "results")
    }

    override fun moveToHintFragment() {
        changeFragment(hintFragment, true, null)
    }

    override fun moveToReplayFragment() {
        changeFragment(replayFragment, false, null)
    }

    override fun moveToTutorialFragment() {
        changeFragment(tutorialFragment, true, null)
    }

    override fun saveWordHistoryFromGameFragment(wordHistory: List<CurrentWord>) {
        viewModel.setWordHistory(wordHistory)
    }

    companion object {
        const val NETWORK_CALL_COMPLETED = "network_call_completed"
    }
}
