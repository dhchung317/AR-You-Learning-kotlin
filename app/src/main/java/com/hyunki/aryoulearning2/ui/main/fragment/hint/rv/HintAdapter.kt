package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.databinding.HintItemViewBinding
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.ModelAdapter.DiffCallback
import com.hyunki.aryoulearning2.ui.main.fragment.category.rv.ModelViewHolder
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject
import kotlin.properties.Delegates

class HintAdapter :
    ListAdapter<Model, HintViewHolder>(
        DiffCallback()
    ) {
    private var modelList: List<Model> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        val binding =
            HintItemViewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return HintViewHolder(binding)
    }

    override fun onBindViewHolder(hintViewHolder: HintViewHolder, i: Int) {
        hintViewHolder.onBind(modelList[i])
    }

    class DiffCallback : DiffUtil.ItemCallback<Model>() {
        override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean =
            oldItem == newItem
    }
}

