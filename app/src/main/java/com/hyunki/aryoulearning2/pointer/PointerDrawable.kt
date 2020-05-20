package com.hyunki.aryoulearning2.pointer

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable

class PointerDrawable : Drawable() {
    private val paint = Paint()
    var isEnabled: Boolean = false

    override fun draw(canvas: Canvas) {
        val cx = (canvas.width / 2).toFloat()
        val cy = (canvas.height / 2).toFloat()
        if (isEnabled) {
            paint.color = Color.GREEN
            canvas.drawCircle(cx, cy, 10f, paint)
        } else {
            paint.color = Color.GRAY
            canvas.drawText("X", cx, cy, paint)
        }
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    @SuppressLint("WrongConstant")
    override fun getOpacity(): Int {
        return 0
    }
}
