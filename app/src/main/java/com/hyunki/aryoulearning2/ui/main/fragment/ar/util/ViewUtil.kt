package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.hyunki.aryoulearning2.R

object ViewUtil {


    fun configureWordContainerTextView(t: TextView, letter: String, font: Typeface?, color: Int): TextView {
        t.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        t.typeface = font
        t.setTextColor(color)
        t.textSize = 100f
        t.text = letter
        t.textAlignment = View.TEXT_ALIGNMENT_CENTER
        return t
    }
}