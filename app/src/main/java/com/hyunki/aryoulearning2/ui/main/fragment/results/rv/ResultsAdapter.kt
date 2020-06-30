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
class ResultsAdapter(private val wordHistory: List<CurrentWord>, private val arModelMap: Map<String, ArModel>, private val pronunUtil: PronunciationUtil, private val TTS: TextToSpeech) : RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ResultsViewHolder {
        return ResultsViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.resultmodel_item,
                        viewGroup, false))
    }

    override fun onBindViewHolder(resultsViewHolder: ResultsViewHolder, position: Int) {
        resultsViewHolder.onBind(wordHistory[position], arModelMap[wordHistory[position].answer]!!, pronunUtil, TTS)
    }

    override fun getItemCount(): Int {
        return wordHistory.size
    }

    inner class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val modelTextView: TextView = itemView.findViewById(R.id.correctmodel_name)
        private val modelImageView: ImageView = itemView.findViewById(R.id.correctmodel_image)
        private val modelAnswer: TextView = itemView.findViewById(R.id.correctmodel_answer)
        private val resultImage: ImageView = itemView.findViewById(R.id.result_imageView)
        private val promptText: TextView = itemView.findViewById(R.id.result_prompt_textView)

        @SuppressLint("ResourceAsColor")
        fun onBind(currentWord: CurrentWord, arModel: ArModel, pronunUtil: PronunciationUtil, TTS: TextToSpeech) {
            val correct = "Correct"

            Log.d("resultsadapter", "onBind currentword: " + currentWord.answer)
            Log.d("resultsadapter", "onBind model: " + arModel.name)
            var wrong = ""
            for (s in currentWord.getAttempts()) {
                wrong += "$s, "
            }

            val name = arModel.name.toUpperCase()[0] + arModel.name.toLowerCase().substring(1)
            val cardView = itemView.findViewById<CardView>(R.id.cardView4)

            modelTextView.text = name

            Picasso.get().load(arModel.image).into(modelImageView)

            if (currentWord.getAttempts().isEmpty()) {
                resultImage.setImageResource(R.drawable.star)
                modelAnswer.text = correct
                promptText.visibility = View.INVISIBLE
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#D81B60"))
                resultImage.setImageResource(R.drawable.error)
                modelAnswer.text = wrong.substring(0, wrong.length - 2)
            }
            //            cardView.setOnClickListener(v -> pronunUtil.textToSpeechAnnouncer(name, TTS));
        }
    }
}
