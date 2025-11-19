package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.databinding.CategoryItemBinding

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) :
    ListAdapter<Category, CategoryViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CategoryViewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        val vh = CategoryViewHolder(binding)
        binding.root.setOnClickListener {
            val category = getItem(vh.bindingAdapterPosition)
            onItemClick(category)
        }
        return vh
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
