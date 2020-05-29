package com.hyunki.aryoulearning2.ui.main.list.rv

import android.content.Context
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.ui.main.controller.NavListener
import com.squareup.picasso.Picasso

import java.util.ArrayList

class ListAdapter : RecyclerView.Adapter<ListAdapter.CategoryViewHolder>() {
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
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(categories[position], listener)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun setLists(categories: List<Category>) {
        Log.d("setLists in list adapter", "setLists: " + categories.size)
        this.categories = categories
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryCard: CardView = itemView.findViewById(R.id.category_card)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)

        fun onBind(category: Category, listener: NavListener?) {
            categoryName.text = category.name
            Picasso.get()
                    .load(category.image)
                    .into(categoryImage)

            Log.d("TAG", category.image)

            categoryCard.setOnClickListener { v ->
                Log.d("list adapter", "onBind: " + category.name)
                if (!category.name.isEmpty()) {
                    Log.d("listadapter", "onBind: onclicklistener " + category.name)
                    listener?.setCategoryFromListFragment(category)
                    listener?.moveToHintFragment()
                }
                makeVibration()
            }
        }

        private fun makeVibration() {
            val categoryVibrate = itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            categoryVibrate.vibrate(100)
        }
    }
}
