package com.hyunki.aryoulearning2.pointer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class PointerDrawable : Drawable() {
    private val paint = Paint()
    var isEnabled: Boolean = false

    override fun draw(canvas: Canvas) {
        val cx = (bounds.width() / 2).toFloat()
        val cy = (bounds.height() / 2).toFloat()
        if (isEnabled) {
            paint.color = Color.GREEN
            canvas.drawCircle(cx, cy, 10f, paint)
        } else {
            paint.color = Color.GRAY
            canvas.drawText("X", cx, cy, paint)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }
}
