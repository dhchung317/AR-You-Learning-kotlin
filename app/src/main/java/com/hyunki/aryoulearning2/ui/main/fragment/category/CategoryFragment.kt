package com.hyunki.aryoulearning2.ui.main.fragment.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.databinding.FragmentListBinding
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.CategoryAdapter
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import javax.inject.Inject

class CategoryFragment @Inject
constructor() : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var listener: NavListener

    override fun onAttach(context: Context) {
        (activity?.application as BaseApplication).appComponent.inject(this)
        if (context is NavListener) {
            listener = context
        }
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
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        mainViewModel.loadCategories()

        mainViewModel.getCatLiveData().observe(viewLifecycleOwner, Observer { categories ->
            renderCategories(categories)
        })
    }


    private fun initRecyclerView(cats: List<Category>) {
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        categoryAdapter = CategoryAdapter({ item ->
            mainViewModel.setCurrentCategory(item.name)
            listener.moveToHintFragment()
        })
        recyclerView.adapter = categoryAdapter
        categoryAdapter.submitList(cats)
    }

    private fun renderCategories(state: MainState) {
        when (state) {
            is MainState.Loading -> {
                showProgressBar(true)
            }

            is MainState.Success.OnCategoriesLoaded -> {
                showProgressBar(false)
                val cats = state.categories
                initRecyclerView(cats)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leak
    }
}
