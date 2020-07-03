package com.hyunki.aryoulearning2.ui.main.fragment.hint

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.databinding.FragmentHintBinding
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.hint.rv.HintAdapter
import com.hyunki.aryoulearning2.util.AutoClearedValue
import com.hyunki.aryoulearning2.util.viewBinding
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import javax.inject.Inject

//TODO refactor/implement pronounciation util
class HintFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) : Fragment(), FragmentListener {
    private val binding by viewBinding(FragmentHintBinding::bind)

    private var hintRecyclerView: RecyclerView? = null
    private var hintAdapter: HintAdapter? = null

    private var listener: NavListener? = null

    private var startGameButton: Button? = null
    private var tutorialButton: Button? = null
    private var backFAB: FloatingActionButton? = null
//    private var progressBar: ProgressBar

    private lateinit var mainViewModel: MainViewModel

    private lateinit var category: String

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hint, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableViews(binding.hintLayout)

        mainViewModel = ViewModelProvider(requireActivity(), viewModelProviderFactory).get(MainViewModel::class.java)

        setUpResultListener()


//
//        mainViewModel.getModelLiveData().observe(viewLifecycleOwner,
//                Observer { state -> renderModelsByCategory(state) })

        //        textToSpeech = pronunciationUtil.getTTS(requireContext());
        initializeViews(view)

        viewClickListeners()

    }

    override fun setCurrentCategoryFromFragment(category: String) {
        parentFragmentManager.setFragmentResult(
                ArHostFragment.REQUEST_KEY,
                bundleOf(ArHostFragment.KEY_ID to category)
        )
    }

    private fun disableViews(view: View?) {
        if (view != null) {
            view.isClickable = false
            if (view is ViewGroup) {
                val vg = view
                for (i in 0 until vg.childCount) {
                    disableViews(vg.getChildAt(i))
                }
            }
        }
    }

    private fun enableViews(view: View?) {
        if (view != null) {
            view.isClickable = true
            if (view is ViewGroup) {
                val vg = view
                for (i in 0 until vg.childCount) {
                    enableViews(vg.getChildAt(i))
                }
            }
        }
    }

    private fun viewClickListeners() {
        startGameButton?.setOnClickListener {
            //            constraintLayout.addView(parentalSupervision);
            //            okButton1.setOnClickListener(v1 -> {
            //                constraintLayout.removeView(parentalSupervision);
            //                constraintLayout.addView(stayAlert);
            //                okButton2.setOnClickListener(v11 -> {
            //                    constraintLayout.removeView(stayAlert);
            listener?.moveToGameFragment()
            disableViews(binding.hintLayout)

            //                });
            //            });
        }

        tutorialButton?.setOnClickListener { listener?.moveToTutorialFragment() }
        backFAB?.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun initializeViews(view: View) {
        Log.d("Leak", "initializeViews: called")
        Log.d("viewbinding", "initializeViews: " + binding.toString())
        startGameButton = binding.hintFragmentButton
        tutorialButton = binding.hintFragTutorialButton
        backFAB = binding.backBtn
        hintAdapter = HintAdapter()
        hintRecyclerView = binding.hintRecyclerView
        hintRecyclerView.let {
            it?.layoutManager = LinearLayoutManager(requireContext().applicationContext, LinearLayoutManager.HORIZONTAL, false)
            it?.adapter = hintAdapter
        }
//        constraintLayout = view.findViewById(R.id.hint_layout)

        //        parentalSupervision = getLayoutInflater().inflate(R.layout.parental_supervision_card, constraintLayout, false);
        //        stayAlert = getLayoutInflater().inflate(R.layout.stay_alert_card, constraintLayout, false);
        //        okButton1 = parentalSupervision.findViewById(R.id.warning_button_ok_1);
        //        okButton2 = stayAlert.findViewById(R.id.warning_button_ok_2);
    }

    private fun renderCurrentCategory(category: String) {
        Log.d("hint", "renderCurrentCategory: render called")
        Log.d("hint", "renderCurrentCategory: " + category)
        mainViewModel.getModelsByCat(category).observe(viewLifecycleOwner, Observer { state ->
//            Log.d("hint", "renderCurrentCategory: " + it.size)
            renderModelsByCategory(state)
        })
    }

    private fun renderModelsByCategory(state: MainState) {
        when (state) {
            is MainState.Loading -> {
//                progressBar.bringToFront()
                showProgressBar(true)
            }
            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                hintAdapter?.arModelList = state.arModels

            }
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
//        if (isVisible) {
//            progressBar.visibility = View.VISIBLE
//        } else {
//            progressBar.visibility = View.GONE
//        }
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
                REQUEST_KEY,
                viewLifecycleOwner,
                FragmentResultListener { requestKey, result ->
                    onFragmentResult(requestKey, result)
                })
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (REQUEST_KEY == requestKey) {
            category = result.getString(KEY_ID)
            renderCurrentCategory(category)
        }
    }

    override fun onDestroyView() {
        backFAB?.setOnClickListener(null)
        backFAB = null
        startGameButton?.setOnClickListener(null)
        startGameButton = null
        tutorialButton?.setOnClickListener(null)
        tutorialButton = null
//        backFAB?.setOnClickListener(null)
//        startGameButton = null

        //nulling listener will cause start game button to die on replay
//        listener = null
//        hintAdapter = null
        setCurrentCategoryFromFragment(category)

        hintRecyclerView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                hintRecyclerView?.adapter = null
            }

            override fun onViewAttachedToWindow(v: View?) {

            }
        })
        super.onDestroyView()
    }

    override fun onDestroy() {
        listener = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        renderCurrentCategory(category)
    }

    companion object {
        const val REQUEST_KEY = "get-current-category"
        const val KEY_ID = "current-category"
    }
}
