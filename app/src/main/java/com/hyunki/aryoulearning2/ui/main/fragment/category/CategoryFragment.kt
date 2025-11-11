package com.hyunki.aryoulearning2.ui.main.fragment.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.databinding.FragmentListBinding
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.CategoryAdapter
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import javax.inject.Inject

class CategoryFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) :
    Fragment(), FragmentListener {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.categoryRv
        mainViewModel = ViewModelProviders.of(requireActivity(), viewModelProviderFactory)
            .get(MainViewModel::class.java)
        mainViewModel.loadCategories()

        mainViewModel.getCatLiveData().observe(viewLifecycleOwner, Observer { categories ->
            renderCategories(categories)
        })
        initRecyclerView()
    }


    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        categoryAdapter = CategoryAdapter(this)
        recyclerView.adapter = categoryAdapter
    }

    private fun renderCategories(state: MainState) {
        when (state) {
            is MainState.Loading -> {
                showProgressBar(true)
            }

            is MainState.Success.OnCategoriesLoaded -> {
                showProgressBar(false)
                categoryAdapter.setLists(state.categories)
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

    override fun setCurrentCategoryFromFragment(category: String) {
        parentFragmentManager.setFragmentResult(
            ArHostFragment.REQUEST_KEY,
            bundleOf(ArHostFragment.KEY_ID to category)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leak
    }

    companion object {
        const val TAG = "ListFragmentX"

    }
}
