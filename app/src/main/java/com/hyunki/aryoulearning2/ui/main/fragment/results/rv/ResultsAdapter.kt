package com.hyunki.aryoulearning2.ui.main.fragment.results.rv

import android.annotation.SuppressLint
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.squareup.picasso.Picasso

//TODO refactor results adapter
class ResultsAdapter(private val wordHistory: List<CurrentWord>, private val pronunUtil: PronunciationUtil) : RecyclerView.Adapter<ResultsViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ResultsViewHolder {
        return ResultsViewHolder(
                itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.resultmodel_item, viewGroup, false))
    }

    override fun onBindViewHolder(resultsViewHolder: ResultsViewHolder, position: Int) {
        resultsViewHolder.onBind(wordHistory[position], pronunUtil)
    }

    override fun getItemCount(): Int {
        return wordHistory.size
    }
}
