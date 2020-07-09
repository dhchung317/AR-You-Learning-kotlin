package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject
import kotlin.properties.Delegates

class HintAdapter : RecyclerView.Adapter<HintViewHolder>() {

    var arModelList: List<ArModel> by Delegates.observable(emptyList()) { _, old, new ->
        notifyChanges(old, new)
    }

    private fun notifyChanges(old: List<ArModel>, new: List<ArModel>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition].name == new[newItemPosition].name
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == new[newItemPosition]
            }

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size
        })

        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        return HintViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(R.layout.hint_item_view, viewGroup, false))
    }

    override fun onBindViewHolder(hintViewHolder: HintViewHolder, i: Int) {
        hintViewHolder.onBind(arModelList[i])
    }

    override fun getItemCount(): Int {
        return arModelList.size
    }

}

