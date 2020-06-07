package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import java.util.ArrayList

class CategoryAdapter(private val fListener: FragmentListener) : RecyclerView.Adapter<CategoryViewHolder>() {
    private var categories: List<Category> = ArrayList()
    private lateinit var listener: NavListener

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CategoryViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.category_item, viewGroup, false)
        val context = viewGroup.context
        if (context is NavListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement FragmentInteractionListener")
        }
        return CategoryViewHolder(view, categories, fListener, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(categories[position], listener)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun setLists(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }
}
