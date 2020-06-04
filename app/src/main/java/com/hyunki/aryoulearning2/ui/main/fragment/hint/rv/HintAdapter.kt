package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject

class HintAdapter @Inject
constructor(private val pronunciationUtil: PronunciationUtil) : RecyclerView.Adapter<HintViewHolder>() {
    private var modelList: List<Model> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        return HintViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(R.layout.hint_item_view, viewGroup, false),
                modelList,
                pronunciationUtil)
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

