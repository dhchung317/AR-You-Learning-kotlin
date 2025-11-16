package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.databinding.CategoryItemBinding
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.util.ArrayList

class CategoryAdapter(private val fListener: FragmentListener) :
    ListAdapter<Category, CategoryViewHolder>(
        DiffCallback()
    ) {
    private val categories: List<Category> = ArrayList()
    private lateinit var listener: NavListener

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CategoryViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup, false)
        val context = viewGroup.context
        if (context is NavListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement FragmentInteractionListener")
        }
        return CategoryViewHolder(binding, categories, fListener, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}
