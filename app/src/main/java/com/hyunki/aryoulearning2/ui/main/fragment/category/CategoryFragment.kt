package com.hyunki.aryoulearning2.ui.main.fragment.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.databinding.FragmentCategoryBinding
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.CategoryAdapter
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.hint.HintFragment
import com.hyunki.aryoulearning2.util.viewBinding
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import javax.inject.Inject

//TODO close button for categoryfragment
class CategoryFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) :
        Fragment(), FragmentListener {
    val binding by viewBinding(FragmentCategoryBinding::bind)

    private lateinit var mainViewModel: MainViewModel

    private var recyclerView: RecyclerView? = null

//    private lateinit var progressBar: ProgressBar

    private var categoryAdapter: CategoryAdapter? = null

    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        progressBar = requireActivity().findViewById(R.id.progress_bar)
        mainViewModel = ViewModelProvider(requireActivity(), viewModelProviderFactory).get(MainViewModel::class.java)
        mainViewModel.getAllCats().observe(viewLifecycleOwner, Observer { renderCategories(it) })
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = binding.categoryRv
        recyclerView.let {
            it?.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            categoryAdapter = CategoryAdapter(this)
            it?.adapter = categoryAdapter
        }

    }

    private fun renderCategories(state: MainState) {
        when (state) {
            is MainState.Loading -> {
                showProgressBar(true)
            }
            is MainState.Error -> showProgressBar(false)
            is MainState.Success.OnCategoriesLoaded -> {
                showProgressBar(false)
                categoryAdapter?.setLists(state.categories)
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

    override fun setCurrentCategoryFromFragment(category: String) {
        parentFragmentManager.setFragmentResult(
                HintFragment.REQUEST_KEY,
                bundleOf(HintFragment.KEY_ID to category)
        )
    }

    override fun onDestroyView() {
        recyclerView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                recyclerView?.adapter = null
            }

            override fun onViewAttachedToWindow(v: View?) {

            }
        })
//        categoryAdapter = null

        super.onDestroyView()
    }

    companion object {
        const val REQUEST_KEY = "get-current-category"
        const val KEY_ID = "current-category"
        const val TAG = "ListFragmentX"
    }
}
