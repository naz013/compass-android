package com.github.naz013.compassapp.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import com.github.naz013.compassapp.theming.Palette

class AngleLabelPainter(private val paint: Paint) {

    private var secondaryColor: Int = Color.BLUE
    private var secondarySolidColor: Int = Color.BLUE
    private val topRect = Rect()
    private val bottomRect = Rect()

    var textSizeDegree = 25f
    var textSize = 15f
    var bounds: Rect = Rect()
    var degree: Float = 0f

    fun updateBounds() {
        if (bounds.width() == 0 || bounds.height() == 0) return

        val topHeight = (bounds.height().toFloat() * TOP_HALF_SIZE).toInt()

        topRect.left = bounds.left
        topRect.top = bounds.top
        topRect.right = bounds.right
        topRect.bottom = bounds.top + topHeight

        bottomRect.left = bounds.left
        bottomRect.top = bounds.top + topHeight
        bottomRect.right = bounds.right
        bottomRect.bottom = bounds.bottom
    }

    fun draw(canvas: Canvas) {
        paint.color = secondaryColor
        paint.textSize = textSizeDegree
        drawText(canvas, degree.toInt().toString() + "°", paint, Gravity.BOTTOM, topRect)

        paint.color = secondarySolidColor
        paint.textSize = textSize
        drawText(canvas, findSub(degree), paint, Gravity.TOP, bottomRect)
    }

    fun setPalette(palette: Palette) {
        secondaryColor = palette.colorSecondary
        secondarySolidColor = palette.colorSecondarySolid
    }

    private fun drawText(canvas: Canvas, text: String, paint: Paint, vGravity: Int, rect: Rect) {
        val r = Rect()
        paint.getTextBounds(text, 0, text.length, r)
        if (vGravity == Gravity.TOP) {
            canvas.drawText(text, rect.centerX().toFloat(), rect.top.toFloat() + r.height(), paint)
        } else {
            canvas.drawText(text, rect.centerX().toFloat(), rect.bottom.toFloat() - r.height() / 2, paint)
        }
    }

    private fun findSub(degree: Float): String {
        RANGES.forEach {
            if (it.range.contains(degree)) {
                return it.label
            }
        }
        return "N"
    }

    data class Angle(var range: ClosedFloatingPointRange<Float>, val label: String)

    companion object {
        private const val TOP_HALF_SIZE = 0.59f
        private val RANGES = listOf(
            Angle(22.5f..67.5f, "NE"),
            Angle(67.5f..112.5f, "E"),
            Angle(112.5f..157.5f, "SE"),
            Angle(157.5f..202.5f, "S"),
            Angle(202.5f..247.5f, "SW"),
            Angle(247.5f..292.5f, "W"),
            Angle(292.5f..337.5f, "NW")
        )
    }
}