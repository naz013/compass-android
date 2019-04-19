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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DottedCompassView : View {

    private var dots: Array<Array<AngledPoint>> = arrayOf()
    private var angleMap: MutableMap<Float, AnglePoints> = mutableMapOf()
    private val paint: Paint = Paint()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var cX: Int = 0
    private var cY: Int = 0
    private val degreesCorrection = -90f
    private var nearAngle = 270f
    var degrees = 0.0f
        set(value) {
            field = value
            nearAngle = findNearestDegree(value)
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
            val points = angleMap[nearAngle]
            Timber.d("onDraw: $nearAngle, $points")
            canvas.save()
            val rotationAngle = degreesCorrection - degrees
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())
            for (i in 0 until dots.size) {
                val dotArray = dots[i]
                val alpha = 255 - (i * 15)
                dotArray.forEach { ap ->
                    val isN = ap.angle == 0f
                    when {
                        isN -> paint.color = Color.RED
                        else -> paint.color = Color.WHITE
                    }
                    paint.alpha = alpha
                    canvas.drawCircle(ap.point.x, ap.point.y, if (isN && ap.ring == 2) 10f else 5f, paint)
                }
            }
            if (points != null) {
                paint.color = Color.GREEN
                points.points.forEach {
                    val alpha = 255 - (it.ring * 15)
                    paint.alpha = alpha
                    canvas.drawCircle(it.point.x, it.point.y, 5f, paint)
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

    private fun findNearestDegree(degree: Float): Float {
        if (angleMap.isEmpty()) return 0f
        val abs = abs(degree)
        var nearestAngle = 0f
        var min = 100f
        angleMap.keys.forEach {
            val diff = abs(it - abs)
            if (diff < min) {
                min = diff
                nearestAngle = it
            }
        }
        Timber.d("findNearestDegree: $degree, $abs, $nearestAngle")
        return nearestAngle
    }

    private fun calculateDots(minRadius: Float, step: Float) {
        val apb = 360f / NUM_OF_DOTS_IN_CIRCLE
        val aH = apb / 2f
        val array = mutableListOf<Array<AngledPoint>>()
        angleMap.clear()
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
                val angledPoint = dotCoordinates(radius, angle, i)
                list.add(angledPoint)
                val anglePoints = if (angleMap.containsKey(angle)) {
                    angleMap[angle] ?: AnglePoints()
                } else {
                    AnglePoints()
                }
                anglePoints.angle = angle
                anglePoints.points.add(angledPoint)
                angleMap[angle] = anglePoints
            }
            Timber.d("calculateDots: $list")
            array.add(list.toTypedArray())
        }
        dots = array.toTypedArray()
        Timber.d("calculateDots: ${angleMap.keys.toList()}")
    }

    private fun dotCoordinates(radius: Float, angle: Float, ring: Int): AngledPoint {
        val x = cX.toFloat() + radius * cos(Math.toRadians(angle.toDouble()))
        val y = cY.toFloat() + radius * sin(Math.toRadians(angle.toDouble()))
        val roundAngle = try {
            String.format(Locale.US, "%.2f", angle).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
        return AngledPoint(ring, roundAngle, PointF(x.toFloat(), y.toFloat()))
    }

    data class AngledPoint(var ring: Int = 0, var angle: Float = 0.0f, var point: PointF = PointF(0.0f, 0.0f))

    data class AnglePoints(var angle: Float = 0.0f, var points: MutableList<AngledPoint> = mutableListOf())

    companion object {
        private const val NUM_OF_CIRCLES = 15
        private const val NUM_OF_DOTS_IN_CIRCLE = 26
    }
}