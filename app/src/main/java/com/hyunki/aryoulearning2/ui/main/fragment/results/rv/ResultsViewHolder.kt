package com.hyunki.aryoulearning2.ui.main.fragment.results.rv

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.databinding.ResultmodelItemBinding
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.squareup.picasso.Picasso

class ResultsViewHolder(
    binding: ResultmodelItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    private val modelTextView: TextView = binding.correctmodelName
    private val modelImageView: ImageView = binding.correctmodelImage
    private val modelAnswer: TextView = binding.correctmodelAnswer
    private val resultImage: ImageView = binding.resultImageView
    private val promptText: TextView = binding.resultPromptTextView

    fun onBind(
        word: CurrentWord,
// TODO: pronunciation
//            pronunUtil: PronunciationUtil,
//            TTS: TextToSpeech
    ) {
        val correct = "Correct"

        var wrong = ""
        for (s in word.attempts) {
            wrong += "$s, "
        }

        val name = word.answer.uppercase()[0] + word.answer.lowercase().substring(1)
        val cardView = itemView.findViewById<CardView>(R.id.cardView4)

        modelTextView.text = name

        Picasso.get().load(word.image).into(modelImageView)

        if (word.attempts.isEmpty()) {
            resultImage.setImageResource(R.drawable.star)
            modelAnswer.text = correct
            promptText.visibility = View.INVISIBLE
        } else {
            cardView.setCardBackgroundColor("#D81B60".toColorInt())
            resultImage.setImageResource(R.drawable.error)
            modelAnswer.text = wrong.dropLast(2)
        }
        // TODO: pronunciation
        // cardView.setOnClickListener(v -> pronunUtil.textToSpeechAnnouncer(name, TTS));
    }
}