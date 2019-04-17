package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin

class DottedCompassView : View {

    private val paint: Paint = Paint()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var cX: Int = 0
    private var cY: Int = 0
    private var mMinRadius: Float = 50f

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
        } else {
            val millis = System.currentTimeMillis()
            val apb = 360f / 26f
            val aH = apb / 2f
            for (i in 0..14) {
                val radius = mMinRadius + i * RADIUS_STEP
                val a = if (i % 2 == 0) {
                    aH
                } else {
                    0f
                }
                paint.alpha = 255 - (i * 15)
                for (j in 0..25) {
                    dotCoordinates(radius, j * apb + a).run {
                        canvas.drawCircle(this.first, this.second, 5f, paint)
                    }
                }
            }
            Timber.d("onDraw: time -> ${System.currentTimeMillis() - millis}")
        }
    }

    private fun dotCoordinates(radius: Float, angle: Float): Pair<Float, Float> {
        val x = cX.toFloat() + radius * cos(Math.toRadians(angle.toDouble()))
        val y = cY.toFloat() + radius * sin(Math.toRadians(angle.toDouble()))
        return Pair(x.toFloat(), y.toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)

        cX = mWidth / 2
        cY = mHeight / 2

        mMinRadius = mWidth.toFloat() * 0.4f / 2f
        Timber.d("onMeasure: radius -> $mMinRadius")
    }

    companion object {
        private const val RADIUS_STEP = 40f
    }
}