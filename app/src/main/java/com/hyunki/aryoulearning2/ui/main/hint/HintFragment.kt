package com.hyunki.aryoulearning2.ui.main.hint

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.State
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.hint.rv.HintAdapter
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import javax.inject.Inject

class HintFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory,
        //    @Inject
        //    PronunciationUtil pronunciationUtil;

            private val hintAdapter: HintAdapter) : Fragment() {

    private var hintRecyclerView: RecyclerView? = null
    private var constraintLayout: ConstraintLayout? = null
    private var listener: NavListener? = null
    private var startGameButton: Button? = null
    private var tutorialButton: Button? = null
    private var backFAB: FloatingActionButton? = null
    private var progressBar: ProgressBar? = null

    private var mainViewModel: MainViewModel? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressBar = activity!!.findViewById(R.id.progress_bar)

    }

    override fun onAttach(context: Context) {
        (activity!!.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hint, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableViews(constraintLayout)

        mainViewModel = ViewModelProviders.of(activity!!, viewModelProviderFactory).get(MainViewModel::class.java)

        mainViewModel!!.curCatLiveData.observe(viewLifecycleOwner, { state -> renderCurrentCategory(state) })
        mainViewModel!!.modelLiveData.observe(viewLifecycleOwner, { state -> renderModelsByCategory(state) })

        mainViewModel!!.loadCurrentCategoryName()
        //        textToSpeech = pronunciationUtil.getTTS(requireContext());
        initializeViews(view)
        hintRecyclerView!!.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)

        hintRecyclerView!!.adapter = hintAdapter
        viewClickListeners()
    }


    private fun disableViews(view: View?) {
        if (view != null) {
            view.isClickable = false
            if (view is ViewGroup) {
                val vg = view as ViewGroup?
                for (i in 0 until vg.getChildCount()) {
                    disableViews(vg.getChildAt(i))
                }
            }
        }
    }

    private fun enableViews(view: View?) {
        if (view != null) {
            view.isClickable = true
            if (view is ViewGroup) {
                val vg = view as ViewGroup?
                for (i in 0 until vg.getChildCount()) {
                    disableViews(vg.getChildAt(i))
                }
            }
        }
    }

    fun viewClickListeners() {

        startGameButton!!.setOnClickListener { v ->
            disableViews(constraintLayout)
            //            constraintLayout.addView(parentalSupervision);
            //            okButton1.setOnClickListener(v1 -> {
            //                constraintLayout.removeView(parentalSupervision);
            //                constraintLayout.addView(stayAlert);
            //                okButton2.setOnClickListener(v11 -> {
            //                    constraintLayout.removeView(stayAlert);
            listener!!.moveToGameFragment()
            //                });
            //            });
        }

        tutorialButton!!.setOnClickListener { v -> listener!!.moveToTutorialFragment() }
        backFAB!!.setOnClickListener { v -> activity!!.onBackPressed() }
    }

    private fun initializeViews(view: View) {
        startGameButton = view.findViewById(R.id.hint_fragment_button)

        hintRecyclerView = view.findViewById(R.id.hint_recycler_view)
        tutorialButton = view.findViewById(R.id.hint_frag_tutorial_button)
        backFAB = view.findViewById(R.id.back_btn)

        constraintLayout = view.findViewById(R.id.hint_layout)

        //        parentalSupervision = getLayoutInflater().inflate(R.layout.parental_supervision_card, constraintLayout, false);
        //        stayAlert = getLayoutInflater().inflate(R.layout.stay_alert_card, constraintLayout, false);
        //        okButton1 = parentalSupervision.findViewById(R.id.warning_button_ok_1);
        //        okButton2 = stayAlert.findViewById(R.id.warning_button_ok_2);

    }

    private fun renderCurrentCategory(state: State) {
        Log.d("rendercurcat", "renderCurrentCategory: " + state.javaClass)

        if (state === State.Loading) {
            showProgressBar(true)

        } else if (state === State.Error) {
            showProgressBar(false)

        } else if (state.javaClass == State.Success.OnCurrentCategoryStringLoaded::class.java) {
            showProgressBar(false)
            val (currentCategoryString) = state as State.Success.OnCurrentCategoryStringLoaded
            mainViewModel!!.loadModelsByCat(currentCategoryString)
            Log.d("hint", "renderCurrentCategory: $currentCategoryString")
        }
    }

    private fun renderModelsByCategory(state: State) {
        if (state === State.Loading) {
            progressBar!!.bringToFront()
            showProgressBar(true)

        } else if (state === State.Error) {
            showProgressBar(false)

        } else if (state.javaClass == State.Success.OnModelsLoaded::class.java) {
            showProgressBar(false)
            val (models) = state as State.Success.OnModelsLoaded
            hintAdapter.setList(models)
        }
    }

    internal fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar!!.visibility = View.VISIBLE
        } else {
            progressBar!!.visibility = View.GONE
        }
    }
}
