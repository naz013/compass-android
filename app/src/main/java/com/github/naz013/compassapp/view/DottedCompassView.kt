package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.github.naz013.compassapp.R
import com.github.naz013.compassapp.theming.Palette
import timber.log.Timber
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DottedCompassView : View {

    private var dots: Array<Array<AngledPoint>> = arrayOf()
    private var angleMap: MutableMap<Float, AnglePoints> = mutableMapOf()

    private val paint = Paint()
    private val textPaint = Paint()
    private val angleLabelPainter = AngleLabelPainter(textPaint)

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var cX: Int = 0
    private var cY: Int = 0
    private val degreesCorrection = -90f
    private var nearAngle = 270f

    private var bgColor = bgColor()
    private var dotColor = dotColor()
    private var northColor = northColor()
    private var currentColor = currentColor()

    var palette: Palette? = null
        set(value) {
            field = value
            if (value != null) {
                updateColors(value)
            }
            invalidate()
        }

    private fun updateColors(palette: Palette) {
        bgColor = bgColor()
        dotColor = dotColor()
        northColor = northColor()
        currentColor = currentColor()
        angleLabelPainter.setPalette(palette)
    }

    var degrees = 0.0f
        set(value) {
            field = value
            nearAngle = findNearestDegree(value)
            angleLabelPainter.degree = value
            invalidate()
        }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.roboto_bold)

        angleLabelPainter.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30F, context.resources.displayMetrics)
        angleLabelPainter.textSizeDegree = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40F, context.resources.displayMetrics)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) {
            super.onDraw(canvas)
        } else {
            canvas.drawColor(bgColor)
            canvas.save()
            val rotationAngle = degreesCorrection - degrees
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())
            for (i in 0 until dots.size) {
                val dotArray = dots[i]
                val alpha = 255 - (i * 15)
                dotArray.forEach { ap ->
                    when {
                        ap.angle == 0f -> paint.color = northColor
                        else -> paint.color = dotColor
                    }
                    paint.alpha = alpha
                    canvas.drawCircle(ap.point.x, ap.point.y, if (ap.isAnchor) 10f else 5f, paint)
                }
            }

            if (nearAngle != 0f) {
                val points = angleMap[nearAngle]
                Timber.d("onDraw: $nearAngle, $points")
                if (points != null) {
                    paint.color = currentColor
                    points.points.forEach {
                        val alpha = 255 - (it.ring * 15)
                        paint.alpha = alpha
                        canvas.drawCircle(it.point.x, it.point.y, 5f, paint)
                    }
                }
            }
            canvas.restore()
            angleLabelPainter.draw(canvas)
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

        val minInt = minRadius.toInt()
        angleLabelPainter.bounds.left = cX - minInt
        angleLabelPainter.bounds.top = cY - minInt
        angleLabelPainter.bounds.right = cX + minInt
        angleLabelPainter.bounds.bottom = cY + minInt
        angleLabelPainter.updateBounds()

        calculateDots(minRadius, radiusStep)
    }

    @ColorInt
    private fun bgColor(): Int = palette?.colorPrimary ?: Color.BLACK

    @ColorInt
    private fun northColor(): Int = palette?.colorSecondary ?: Color.RED

    @ColorInt
    private fun currentColor(): Int = palette?.colorSecondarySolid ?: Color.MAGENTA

    @ColorInt
    private fun dotColor(): Int = palette?.colorOnPrimary ?: Color.WHITE

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
            array.add(list.toTypedArray())
        }
        dots = array.toTypedArray()
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
        return AngledPoint(ring, roundAngle, PointF(x.toFloat(), y.toFloat()), roundAngle == 0f && ring == 4)
    }

    data class AngledPoint(var ring: Int = 0, var angle: Float = 0.0f, var point: PointF = PointF(0.0f, 0.0f), var isAnchor: Boolean = false)

    data class AnglePoints(var angle: Float = 0.0f, var points: MutableList<AngledPoint> = mutableListOf())

    companion object {
        private const val NUM_OF_CIRCLES = 15
        private const val NUM_OF_DOTS_IN_CIRCLE = 26
    }
}