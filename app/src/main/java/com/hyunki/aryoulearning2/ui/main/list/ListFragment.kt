package com.hyunki.aryoulearning2.ui.main.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.State
import com.hyunki.aryoulearning2.ui.main.list.rv.ListAdapter
import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import javax.inject.Inject

class ListFragment @Inject
constructor(private val viewModelProviderFactory: ViewModelProviderFactory, private val listAdapter: ListAdapter) : Fragment() {

    private var mainViewModel: MainViewModel? = null

    private var recyclerView: RecyclerView? = null

    private var progressBar: ProgressBar? = null


    override fun onAttach(context: Context) {
        (activity!!.application as BaseApplication).appComponent.inject(this)
        super.onAttach(context)
        Log.d(TAG, "onAttach: on attach ran")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressBar = activity!!.findViewById(R.id.progress_bar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, " on create ran")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, " on viewcreated ran")

        recyclerView = view.findViewById(R.id.category_rv)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelProviderFactory).get(MainViewModel::class.java)
        mainViewModel!!.loadCategories()

        mainViewModel!!.catLiveData.observe(viewLifecycleOwner, Observer { categories ->
            renderCategories(categories)
            Log.d(TAG, "onViewCreated: " + mainViewModel!!.catLiveData.value!!.javaClass)

        })
        initRecyclerView()
    }


    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext(),
                RecyclerView.HORIZONTAL,
                false)
        recyclerView!!.adapter = listAdapter
    }

    private fun renderCategories(state: State) {
        if (state === State.Loading) {
            progressBar!!.bringToFront()
            showProgressBar(true)

        } else if (state === State.Error) {
            showProgressBar(false)

        } else if (state.javaClass == State.Success.OnCategoriesLoaded::class.java) {
            showProgressBar(false)
            val (categories) = state as State.Success.OnCategoriesLoaded
            listAdapter.setLists(categories)

            Log.d(TAG, "renderCategories: " + categories.size)
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
            progressBar!!.visibility = View.VISIBLE
        } else {
            progressBar!!.visibility = View.GONE
        }
    }

    companion object {
        val TAG = "ListFragmentX"
    }

    //    @Override
    //    public void onResume() {
    //        super.onResume();
    //        renderCategories(mainViewModel.getCatLiveData().getValue());
    //    }
}
