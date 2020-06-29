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

class ValidatorCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CardView(context, attrs){
    private val attributes: TypedArray

    private lateinit var wordValidator: TextView
    private lateinit var validatorWord: TextView
    private lateinit var validatorWrongWord: TextView
    private lateinit var validatorWrongPrompt: TextView
    private lateinit var validatorImage: ImageView
    private lateinit var validatorBackgroundImage: ImageView
    private lateinit var validatorOkButton: Button

    init {
        inflate(context, R.layout.validator_card, this)
        attributes = context.obtainStyledAttributes(attrs, R.styleable.ValidatorCardView)

    }

}