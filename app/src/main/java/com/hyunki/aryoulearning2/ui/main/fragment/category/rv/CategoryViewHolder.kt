package com.hyunki.aryoulearning2.ui.main.fragment.category.rv

import android.content.Context
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.ui.main.fragment.controller.FragmentListener
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import com.hyunki.aryoulearning2.util.AutoClearedValue
import com.squareup.picasso.Picasso

class CategoryViewHolder(itemView: View, private val categoryList: List<Category>, fListener: FragmentListener, nListener: NavListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val categoryName: TextView = itemView.findViewById(R.id.category_name)
    private val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
    private var fListener: FragmentListener? = fListener
    private var nListener: NavListener? = nListener

    init {
        itemView.setOnClickListener(this)
    }

    fun onBind(category: Category) {
        categoryName.text = category.name
        Picasso.get()
                .load(category.image)
                .into(categoryImage)
    }

    private fun makeVibration() {
        val categoryVibrate = itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        categoryVibrate.vibrate(100)
    }

    override fun onClick(v: View?) {
        val category = categoryList[adapterPosition]
        if (category.name.isNotEmpty()) {
            fListener?.setCurrentCategoryFromFragment(category.name)
            nListener?.moveToHintFragment()
            fListener = null
            nListener = null
        }
        makeVibration()
    }
}