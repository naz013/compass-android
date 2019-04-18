package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import timber.log.Timber
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DottedCompassView : View {

    private var dots: Array<Array<AngledPoint>> = arrayOf()
    private var angleMap: Map<Float, AnglePoints> = mapOf()
    private val paint: Paint = Paint()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var cX: Int = 0
    private var cY: Int = 0
    private val degreesCorrection = -90f
    var degrees = 0.0f
        set(value) {
            field = value
            invalidate()
        }

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
//            val millis = System.currentTimeMillis()
            canvas.save()
            val rotationAngle = degreesCorrection - degrees
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())
            for (i in 0 until dots.size) {
                val dotArray = dots[i]
                val alpha = 255 - (i * 15)
                dotArray.forEach { ap ->
                    when {
                        ap.angle == 0f -> paint.color = Color.RED
                        else -> paint.color = Color.WHITE
                    }
                    paint.alpha = alpha
                    canvas.drawCircle(ap.point.x, ap.point.y, 5f, paint)
                }
            }
            canvas.restore()
//            Timber.d("onDraw: $degrees, $rotationAngle, time -> ${System.currentTimeMillis() - millis}")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)

        cX = mWidth / 2
        cY = mHeight / 2

        val minRadius = mWidth.toFloat() * 0.4f / 2f
        val radiusStep = (((max(mWidth, mHeight) * 1.3f) - (minRadius * 2f)) / 2f) / NUM_OF_CIRCLES
        Timber.d("onMeasure: radius -> $minRadius, $radiusStep")

        calculateDots(minRadius, radiusStep)
    }

    private fun calculateDots(minRadius: Float, step: Float) {
        val apb = 360f / NUM_OF_DOTS_IN_CIRCLE
        val aH = apb / 2f
        val array = mutableListOf<Array<AngledPoint>>()
        val map = mutableMapOf<Float, AnglePoints>()
        for (i in 0 until NUM_OF_CIRCLES) {
            val radius = minRadius + i * step
            val a = if (i % 2 == 0) {
                0f
            } else {
                aH
            }
            val list = mutableListOf<AngledPoint>()
            for (j in 0 until NUM_OF_DOTS_IN_CIRCLE) {
                val angle = j * apb + a
                val angledPoint = dotCoordinates(radius, angle)
                list.add(angledPoint)
                val anglePoints = if (map.containsKey(angle)) {
                    map[angle] ?: AnglePoints()
                } else {
                    AnglePoints()
                }
                anglePoints.points.add(angledPoint.point)
            }
//            Timber.d("calculateDots: $list")
            array.add(list.toTypedArray())
        }
        dots = array.toTypedArray()
    }

    private fun dotCoordinates(radius: Float, angle: Float): AngledPoint {
        val x = cX.toFloat() + radius * cos(Math.toRadians(angle.toDouble()))
        val y = cY.toFloat() + radius * sin(Math.toRadians(angle.toDouble()))
        val roundAngle = try {
            String.format(Locale.US, "%.2f", angle).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
        return AngledPoint(roundAngle, PointF(x.toFloat(), y.toFloat()))
    }

    data class AngledPoint(var angle: Float = 0.0f, var point: PointF = PointF(0.0f, 0.0f))

    data class AnglePoints(var angle: Float = 0.0f, var points: MutableList<PointF> = mutableListOf())

    companion object {
        private const val NUM_OF_CIRCLES = 15
        private const val NUM_OF_DOTS_IN_CIRCLE = 26
    }
}