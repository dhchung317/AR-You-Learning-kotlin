package com.hyunki.aryoulearning2.ui.main.fragment.results.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.createBitmap

fun captureRecyclerView(rv: RecyclerView): Bitmap {
    val adapter = rv.adapter ?: error("RecyclerView has no adapter")
    val itemCount = adapter.itemCount

    val paint = Paint()
    var totalHeight = 0
    val bitmaps = ArrayList<Bitmap>(itemCount)

    // Create and bind each item view, render it to a bitmap
    for (i in 0 until itemCount) {
        val holder = adapter.createViewHolder(rv, adapter.getItemViewType(i))
        adapter.onBindViewHolder(holder, i)

        val itemView = holder.itemView

        // Measure/layout offscreen
        itemView.measure(
            View.MeasureSpec.makeMeasureSpec(rv.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        itemView.layout(0, 0, itemView.measuredWidth, itemView.measuredHeight)

        val itemBitmap = createBitmap(itemView.measuredWidth, itemView.measuredHeight)
        val canvas = Canvas(itemBitmap)
        itemView.draw(canvas)

        bitmaps.add(itemBitmap)
        totalHeight += itemView.measuredHeight
    }

    // Stitch all item bitmaps together
    val bigBitmap = createBitmap(rv.measuredWidth, totalHeight)
    val bigCanvas = Canvas(bigBitmap)

    var top = 0f
    for (bmp in bitmaps) {
        bigCanvas.drawBitmap(bmp, 0f, top, paint)
        top += bmp.height
        bmp.recycle()
    }

    return bigBitmap
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