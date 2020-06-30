package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import java.util.ArrayList

import javax.inject.Inject

class HintAdapter @Inject
constructor(private val pronunciationUtil: PronunciationUtil) : RecyclerView.Adapter<HintViewHolder>() {
    private var arModelList: List<ArModel> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        return HintViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(R.layout.hint_item_view, viewGroup, false),
                arModelList,
                pronunciationUtil)
    }

    override fun onBindViewHolder(hintViewHolder: HintViewHolder, i: Int) {
        hintViewHolder.onBind(arModelList[i])
    }

    override fun getItemCount(): Int {
        return arModelList.size
    }

    fun setList(arModelList: List<ArModel>) {
        this.arModelList = arModelList
        notifyDataSetChanged()
    }
}

