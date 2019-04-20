package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.github.naz013.compassapp.theming.Palette
import timber.log.Timber

class SimpleOneCompassView : BaseCompassView {

    private val paint = Paint()

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var cX: Int = 0
    private var cY: Int = 0
    private val degreesCorrection = -90f

    private var bgColor = bgColor()
    private var lineColor = dotColor()
    private var northColor = northColor()
    private var currentColor = currentColor()

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE


    }

    override fun updateColors(palette: Palette) {
        super.updateColors(palette)
        bgColor = bgColor()
        lineColor = dotColor()
        northColor = northColor()
        currentColor = currentColor()
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
        } else {
            canvas.drawColor(bgColor)
            canvas.save()
            val rotationAngle = degreesCorrection - degrees
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())

            canvas.restore()
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)

        cX = mWidth / 2
        cY = mHeight / 2

        val minRadius = mWidth.toFloat() * 0.4f / 2f
        Timber.d("onMeasure: radius -> $minRadius, ")

        setupText(cX, cY, minRadius.toInt())
        calculateLines(minRadius, 0f)
    }

    @ColorInt
    private fun bgColor(): Int = palette?.colorPrimary ?: Color.BLACK

    @ColorInt
    private fun northColor(): Int = palette?.colorSecondary ?: Color.RED

    @ColorInt
    private fun currentColor(): Int = palette?.colorSecondarySolid ?: Color.MAGENTA

    @ColorInt
    private fun dotColor(): Int = palette?.colorOnPrimary ?: Color.WHITE


    private fun calculateLines(minRadius: Float, step: Float) {

    }

    data class AngledLine(var ring: Int = 0, var angle: Float = 0.0f, var point: PointF = PointF(0.0f, 0.0f), var isAnchor: Boolean = false)

    companion object {
    }
}