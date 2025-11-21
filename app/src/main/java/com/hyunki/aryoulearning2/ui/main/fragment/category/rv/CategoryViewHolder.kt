package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.databinding.CategoryItemBinding
import com.squareup.picasso.Picasso

class CategoryViewHolder(
    val binding: CategoryItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(category: Category) {
        binding.categoryName.text = category.name
        Picasso.get()
            .load(category.image)
            .into(binding.categoryImage)
    }
}