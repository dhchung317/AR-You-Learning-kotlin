package com.hyunki.aryoulearning2.ui.main.fragment.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.hyunki.aryoulearning2.util.AutoClearedValue
import com.hyunki.aryoulearning2.util.viewBinding
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory
import java.lang.ref.WeakReference
import javax.inject.Inject

//TODO close button for categoryfragment
//TODO progressbar
class CategoryFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory) : Fragment(), FragmentListener {
    private val binding by viewBinding(FragmentCategoryBinding::bind)

    private lateinit var mainViewModel: MainViewModel

    private var progressBar: ProgressBar by AutoClearedValue()
    private var categoryAdapter: CategoryAdapter by AutoClearedValue()
    private var recyclerView: RecyclerView by AutoClearedValue()


    override fun onAttach(context: Context) {
        (requireActivity().application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar = requireActivity().findViewById(R.id.progress_bar)
        mainViewModel = ViewModelProvider(requireActivity(), viewModelProviderFactory).get(MainViewModel::class.java)
        mainViewModel.getAllCats().observe(viewLifecycleOwner, Observer { renderCategories(it) })
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView = binding.categoryRv
        recyclerView.let {
            it.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            categoryAdapter = CategoryAdapter(WeakReference(this))
            it.adapter = categoryAdapter
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
                HintFragment.REQUEST_KEY,
                bundleOf(HintFragment.KEY_ID to category)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        const val TAG = "ListFragmentX"
    }
}
