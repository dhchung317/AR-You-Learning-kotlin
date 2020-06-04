package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.animation.Animations
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.squareup.picasso.Picasso

import java.util.ArrayList

import javax.inject.Inject

class HintAdapter @Inject
constructor(private val pronunciationUtil: PronunciationUtil) : RecyclerView.Adapter<HintAdapter.HintViewHolder>() {
    private var modelList: List<Model> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HintViewHolder {
        return HintViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.hint_item_view, viewGroup, false))
    }

    override fun onBindViewHolder(hintViewHolder: HintViewHolder, i: Int) {
        hintViewHolder.onBind(modelList[i])
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    inner class HintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        private val imageView: ImageView = itemView.findViewById<ImageView>(R.id.hint_fragment_image_view)
        private val textView: TextView = itemView.findViewById<TextView>(R.id.hint_fragment_textview)

        init{
            itemView.setOnClickListener(this)
        }

        fun onBind(model: Model) {
            textView.setTextColor(Color.DKGRAY)
            Picasso.get().load(model.image).into(imageView)
            textView.text = model.name

        }

        override fun onClick(v: View?) {
            val model = modelList[adapterPosition]
                pronunciationUtil.textToSpeechAnnouncer(model.name, pronunciationUtil.textToSpeech)
                itemView.startAnimation(Animations.Normal().getVibrator(itemView))
                textView.setTextColor(Color.LTGRAY)
                val timer = object : CountDownTimer(1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        textView.setTextColor(Color.DKGRAY)
                        itemView.clearAnimation()
                    }
                }
                timer.start()
        }
    }

    fun setList(modelList: List<Model>) {
        this.modelList = modelList
        notifyDataSetChanged()
    }
}

