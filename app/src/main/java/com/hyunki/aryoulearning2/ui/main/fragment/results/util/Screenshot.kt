package com.hyunki.aryoulearning2.ui.main.fragment.results.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView

fun captureRv(rv: RecyclerView): Bitmap {

    rv.measure(
        View.MeasureSpec.makeMeasureSpec(rv.getWidth(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    val bm = Bitmap.createBitmap(
        rv.getWidth(),
        rv.getMeasuredHeight(),
        Bitmap.Config.ARGB_8888
    )
    rv.draw(Canvas(bm))
    return bm
}
fun captureLayout(v: ConstraintLayout?): Bitmap {
    val bmp = createBitmap(v?.width ?: 0, v?.height ?: 0)
    val c = Canvas(bmp)
    c.drawColor(Color.WHITE) // avoid black background when saving JPG
    v?.draw(c)
    return bmp
}

fun combineBitmaps(topBmp: Bitmap, listBmp: Bitmap): Bitmap {
    val width = maxOf(topBmp.width, listBmp.width)
    val height = topBmp.height + listBmp.height

    val result = createBitmap(width, height)
    val canvas = Canvas(result)
    canvas.drawColor(Color.WHITE)

    var y = 0f
    canvas.drawBitmap(topBmp, 0f, y, null)
    y += topBmp.height
    canvas.drawBitmap(listBmp, 0f, y, null)

    return result
}