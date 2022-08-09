package com.heena.user.extras

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class CustomMultipleDotSpan(private val radius: Float, private var color: IntArray) :
    LineBackgroundSpan {


    override fun drawBackground(canvas: Canvas, paint: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, charSequence: CharSequence, start: Int, end: Int, lineNum: Int) {
        val total = if (color.size > 5) 5 else color.size
        var leftMost = (total - 1) * -10
        for (i in 0 until total) {
            val oldColor: Int = paint.color
            if (color[i] != 0) {
                paint.color = color[i]
            }
            canvas.drawCircle(((left + right) / 2 - leftMost).toFloat(), bottom + radius, radius, paint)
            paint.color = oldColor
            leftMost += 20
        }
    }
}