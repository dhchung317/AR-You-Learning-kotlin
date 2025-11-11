package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject
import kotlin.properties.Delegates

class HintAdapter : RecyclerView.Adapter<HintViewHolder>() {
//    private var modelList: List<Model> = ArrayList()

    var modelList: List<Model> by Delegates.observable(emptyList()) { _, old, new ->
        notifyChanges(old, new)
    }

    fun notifyChanges(old: List<Model>, new: List<Model>) {

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
        hintViewHolder.onBind(modelList[i])
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    fun setList(modelList: List<Model>) {
        this.modelList = modelList
        notifyDataSetChanged()
    }
}

