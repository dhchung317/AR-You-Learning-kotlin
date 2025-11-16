package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.databinding.CategoryItemBinding
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.squareup.picasso.Picasso

class CategoryViewHolder(
    val binding: CategoryItemBinding,
    private val categoryList: List<Category>,
    private val fListener: FragmentListener,
    private val nListener: NavListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    init {
        itemView.setOnClickListener(this)
    }

    fun onBind(category: Category) {
        binding.categoryName.text = category.name
        Picasso.get()
            .load(category.image)
            .into(binding.categoryImage)
    }

    override fun onClick(v: View?) {
        val category = categoryList[bindingAdapterPosition]
        if (!category.name.isEmpty()) {
            fListener.setCurrentCategoryFromFragment(category.name)
            nListener.moveToHintFragment()
        }
    }
}