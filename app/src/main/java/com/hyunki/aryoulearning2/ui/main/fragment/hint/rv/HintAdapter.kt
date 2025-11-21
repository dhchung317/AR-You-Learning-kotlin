package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.databinding.HintItemViewBinding

class HintAdapter :
    ListAdapter<Model, HintViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        val binding =
            HintItemViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return HintViewHolder(binding)
    }

    override fun onBindViewHolder(hintViewHolder: HintViewHolder, i: Int) {
        hintViewHolder.onBind(getItem(i))
    }

    class DiffCallback : DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean =
            oldItem == newItem
    }
}

