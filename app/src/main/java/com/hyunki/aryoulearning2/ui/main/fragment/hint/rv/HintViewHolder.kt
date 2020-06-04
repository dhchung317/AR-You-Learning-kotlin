package com.hyunki.aryoulearning2.ui.main.fragment.hint.rv

import android.graphics.Color
import android.os.CountDownTimer
import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.animation.Animations
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil
import com.squareup.picasso.Picasso

class HintViewHolder(itemView: View, private val modelList: List<Model>, val pronunciationUtil: PronunciationUtil) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private val imageView: ImageView = itemView.findViewById<ImageView>(R.id.hint_fragment_image_view)
    private val textView: TextView = itemView.findViewById<TextView>(R.id.hint_fragment_textview)

    init {
        itemView.setOnClickListener(this)
    }

    fun onBind(model: Model) {
        textView.setTextColor(Color.DKGRAY)
        Picasso.get().load(model.image).into(imageView)
        textView.text = model.name
    }

    override fun onClick(v: View?) {
        toggleViews(v,false)
        val model = modelList[adapterPosition]
        pronunciationUtil.textToSpeechAnnouncer(model.name, pronunciationUtil.textToSpeech)
        itemView.startAnimation(Animations.Normal().getVibrator(itemView))
        textView.setTextColor(Color.LTGRAY)
        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                textView.setTextColor(Color.DKGRAY)
                itemView.clearAnimation()
                toggleViews(v,true)
            }
        }
        timer.start()
    }

    private fun toggleViews(v:View?, isClickable: Boolean){
        for (v in v?.parent as ViewGroup){
            v.isClickable = isClickable
        }
    }
}