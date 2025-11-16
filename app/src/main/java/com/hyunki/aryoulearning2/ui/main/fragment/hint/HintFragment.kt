package com.hyunki.aryoulearning2.ui.main.fragment.hint

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.databinding.FragmentHintBinding
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.ui.main.fragment.hint.rv.HintAdapter
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import javax.inject.Inject

//TODO refactor/implement pronounciation util
class HintFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) : Fragment(),
    FragmentListener {
    private var _binding: FragmentHintBinding? = null
    private val binding get() = _binding!!
    private lateinit var hintRecyclerView: RecyclerView
    private val hintAdapter = HintAdapter()
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var listener: NavListener
    private lateinit var startGameButton: Button
    private lateinit var tutorialButton: Button
    private lateinit var backFAB: FloatingActionButton
    private lateinit var mainViewModel: MainViewModel
    private lateinit var category: String


    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        if (context is NavListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setUpResultListener()
        _binding = FragmentHintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintLayout = binding.hintLayout
        enableViews(constraintLayout)

        mainViewModel = ViewModelProvider(
            requireActivity(),
            viewModelProviderFactory
        ).get(MainViewModel::class.java)

        mainViewModel.getModelLiveData().observe(
            viewLifecycleOwner,
            Observer { state -> renderModelsByCategory(state) })
        //        textToSpeech = pronunciationUtil.getTTS(requireContext());
        initializeViews()
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
                    disableViews(vg.getChildAt(i))
                }
            }
        }
    }

    private fun viewClickListeners() {
        startGameButton.setOnClickListener {
            disableViews(constraintLayout)
            //            constraintLayout.addView(parentalSupervision);
            //            okButton1.setOnClickListener(v1 -> {
            //                constraintLayout.removeView(parentalSupervision);
            //                constraintLayout.addView(stayAlert);
            //                okButton2.setOnClickListener(v11 -> {
            //                    constraintLayout.removeView(stayAlert);
            listener.moveToGameFragment()

            //                });
            //            });
        }

        tutorialButton.setOnClickListener { listener.moveToTutorialFragment() }
        backFAB.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun initializeViews() {
        startGameButton = binding.hintFragmentButton

        hintRecyclerView = binding.hintRecyclerView
        tutorialButton = binding.hintFragTutorialButton
        backFAB = binding.backBtn
        hintRecyclerView = binding.hintRecyclerView
        hintRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        hintRecyclerView.adapter = hintAdapter
        constraintLayout = binding.hintLayout

        //        parentalSupervision = getLayoutInflater().inflate(R.layout.parental_supervision_card, constraintLayout, false);
        //        stayAlert = getLayoutInflater().inflate(R.layout.stay_alert_card, constraintLayout, false);
        //        okButton1 = parentalSupervision.findViewById(R.id.warning_button_ok_1);
        //        okButton2 = stayAlert.findViewById(R.id.warning_button_ok_2);
    }

    private fun renderCurrentCategory(category: String) {
        mainViewModel.loadModelsByCat(category)
    }

    private fun renderModelsByCategory(state: MainState) {
        when (state) {
            is MainState.Loading -> {
                showProgressBar(true)
            }

            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnModelsLoaded -> {
                showProgressBar(false)
                hintAdapter.submitList(state.models)
            }
            else -> showProgressBar(false)
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            (requireActivity() as MainActivity).showProgressBar(true)
        } else {
            (requireActivity() as MainActivity).showProgressBar(false)
        }
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
            ArHostFragment.REQUEST_KEY,
            this
        ) { requestKey, result ->
            onFragmentResult(requestKey, result)
        }
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        if (REQUEST_KEY == requestKey) {
            category = result.getString(KEY_ID) ?: "Animals"
            renderCurrentCategory(category)
        }
    }

    override fun onDestroyView() {
        hintRecyclerView.adapter = null
        setCurrentCategoryFromFragment(category)
        _binding = null // prevent memory leak
        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "get-current-category"
        const val KEY_ID = "current-category"
    }
}
