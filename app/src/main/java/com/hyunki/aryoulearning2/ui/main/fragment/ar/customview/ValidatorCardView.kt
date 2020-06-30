package com.hyunki.aryoulearning2.ui.main.fragment.ar.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.hyunki.aryoulearning2.R
import com.squareup.picasso.Picasso
import kotlin.properties.Delegates.observable

class ValidatorCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CardView(context, attrs) {
    private val attributes: TypedArray

    private val headerTextView: TextView by lazy { findViewById<TextView>(R.id.validator_header) }
    private val answerPromptTextView: TextView by lazy { findViewById<TextView>(R.id.validator_answer_prompt) }
    private val answerTextView: TextView by lazy { findViewById<TextView>(R.id.answer) }
    private val wrongAnswerPromptTextView: TextView by lazy { findViewById<TextView>(R.id.validator_wrong_answer_prompt) }
    private val wrongAnswerTextView: TextView by lazy { findViewById<TextView>(R.id.wrong_answer) }
    private val answerImageView: ImageView by lazy { findViewById<ImageView>(R.id.answer_imageView) }
    private val backgroundImageView: ImageView by lazy { findViewById<ImageView>(R.id.validator_background_imageView) }
    val okButton: Button by lazy { findViewById<Button>(R.id.validator_ok_button) }

    var headerText: String by observable(initialValue = "") { _, _, newValue ->
        headerTextView.text = newValue
    }
    var answerText: String by observable(initialValue = "") { _, _, newValue ->
        answerTextView.text = newValue
    }
    var answerPromptText: String by observable(initialValue = "") { _, _, newValue ->
        answerPromptTextView.text = newValue
    }
    var wrongAnswerText: String by observable(initialValue = "") { _, _, newValue ->
        wrongAnswerTextView.text = newValue
    }
    var wrongAnswerPromptText: String by observable(initialValue = "") { _, _, newValue ->
        wrongAnswerPromptTextView.text = newValue
    }
    var answerImage: String by observable(initialValue = "") { _, _, newValue ->
        Picasso.get().load(newValue).into(answerImageView)
    }
    var backgroundImage: Int by observable(initialValue = 0) { _, _, newValue ->
        backgroundImageView.setImageResource(newValue)
    }
    var wrongAnswerVisibility: Int by observable(initialValue = View.INVISIBLE) {_,_,newValue ->
        wrongAnswerTextView.visibility = newValue
    }
    var wrongAnswerPromptVisibility: Int by observable(initialValue = View.INVISIBLE) {_,_,newValue ->
        wrongAnswerPromptTextView.visibility = newValue
    }

    init {
        inflate(context, R.layout.validator_card, this)
        attributes = context.obtainStyledAttributes(attrs, R.styleable.ValidatorCardView)
    }

}