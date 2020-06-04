package com.hyunki.aryoulearning2.ui.main.fragment.category

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.CategoryAdapter
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import javax.inject.Inject

class CategoryFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) :
        Fragment(), FragmentListener {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var recyclerView: RecyclerView

    private lateinit var progressBar: ProgressBar

    private lateinit var categoryAdapter: CategoryAdapter


    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar = requireActivity().findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.category_rv)
        mainViewModel = ViewModelProviders.of(requireActivity(), viewModelProviderFactory).get(MainViewModel::class.java)
        mainViewModel.loadCategories()

        mainViewModel.getCatLiveData().observe(viewLifecycleOwner, Observer { categories ->
            renderCategories(categories)
            Log.d(TAG, "onViewCreated: " + mainViewModel.getCatLiveData().value!!.javaClass)

        })
        initRecyclerView()
    }


    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),
                RecyclerView.HORIZONTAL,
                false)
        categoryAdapter = CategoryAdapter(this)
        recyclerView.adapter = categoryAdapter
    }

    private fun renderCategories(state: MainState) {
        when (state) {
            is MainState.Loading -> {
//                progressBar.bringToFront()
                showProgressBar(true)
            }
            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnCategoriesLoaded -> {
                showProgressBar(false)
                categoryAdapter.setLists(state.categories)
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

    override fun setCurrentCategoryFromFragment(category: String) {
        parentFragmentManager.setFragmentResult(
                ArHostFragment.REQUEST_KEY,
                bundleOf(ArHostFragment.KEY_ID to category)
        )
    }

    companion object {
        const val TAG = "ListFragmentX"

    }

    //    @Override
    //    public void onResume() {
    //        super.onResume();
    //        renderCategories(mainViewModel.getCatLiveData().getValue());
    //    }
}
