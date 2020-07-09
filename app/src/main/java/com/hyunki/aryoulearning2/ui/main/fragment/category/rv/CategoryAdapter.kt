package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import java.util.ArrayList
import kotlin.properties.Delegates

class CategoryAdapter(private val fListener: FragmentListener) : RecyclerView.Adapter<CategoryViewHolder>() {
    var categories: List<Category> = emptyList()
//            by Delegates.observable(emptyList()) { _, old, new ->
//        notifyChanges(old, new)
//    }
    private lateinit var listener: NavListener

    private fun notifyChanges(old: List<Category>, new: List<Category>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition].hashCode() == new[newItemPosition].hashCode()
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == new[newItemPosition]
            }

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size
        })

        diff.dispatchUpdatesTo(this)
    }

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
        holder.onBind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }
//
    fun setLists(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }


}
