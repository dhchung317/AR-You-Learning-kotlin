package com.hyunki.aryoulearning2.ui.main.fragment.results.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hyunki.aryoulearning2.databinding.ResultmodelItemBinding

class ResultsAdapter(
    private val wordHistory: List<CurrentWord>,
    private val modelMap: Map<String, Model>
// TODO: pronunciation
//    private val pronunUtil: PronunciationUtil,
//    private val TTS: TextToSpeech
) : ListAdapter<CurrentWord, ResultsViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ResultsViewHolder {
        val binding =
            ResultmodelItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ResultsViewHolder(binding)
    }

    override fun onBindViewHolder(resultsViewHolder: ResultsViewHolder, position: Int) {
        resultsViewHolder.onBind(
            wordHistory[position],
            modelMap[wordHistory[position].answer]!!,
// TODO: pronunciation
//            pronunUtil,
//            TTS
        )
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentWord>() {
        override fun areItemsTheSame(oldItem: CurrentWord, newItem: CurrentWord): Boolean =
            oldItem.answer == newItem.answer

        override fun areContentsTheSame(oldItem: CurrentWord, newItem: CurrentWord): Boolean =
            oldItem.answer == newItem.answer
    }
}
