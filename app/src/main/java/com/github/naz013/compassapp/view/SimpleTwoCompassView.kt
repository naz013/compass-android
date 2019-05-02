package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.github.naz013.compassapp.theming.Palette
import com.github.naz013.compassapp.utils.ViewUtils
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class SimpleTwoCompassView : BaseCompassView {

    private val paint = Paint()
    private var angleMap: MutableMap<Float, AngledLine> = mutableMapOf()
    private var arrow: Arrow? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mShortLineLength: Float = 20f
    private var mLongLineLength: Float = 50f
    private var mNorthRadius: Float = 10f
    private var cX: Int = 0
    private var cY: Int = 0
    private val degreesCorrection = -90f

    private var bgColor = bgColor()
    private var lineColor = dotColor()
    private var northColor = northColor()
    private var currentColor = currentColor()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mShortLineLength = ViewUtils.dp2px(context, 15).toFloat()
        mLongLineLength = ViewUtils.dp2px(context, 40).toFloat()
        mNorthRadius = mShortLineLength / 2f

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.strokeWidth = ViewUtils.dp2px(context, 2).toFloat()
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

            val rotationAngle = degreesCorrection - degrees

            canvas.save()
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())

            angleMap.values.forEach { al ->
                if (al.isAnchor) {
                    paint.color = northColor
                    arrow?.let { ar ->
                        canvas.drawLine(ar.start.x, ar.start.y, ar.left.x, ar.left.y, paint)
                        canvas.drawLine(ar.start.x, ar.start.y, ar.right.x, ar.right.y, paint)
                    }
                } else {
                    paint.color = lineColor
                    canvas.drawLine(al.start.x, al.start.y, al.end.x, al.end.y, paint)
                }
            }

            canvas.restore()

            paint.color = currentColor
            canvas.drawLine(cX.toFloat(), 0f, cX.toFloat(), mLongLineLength * 2f, paint)
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)

        cX = mWidth / 2
        cY = mHeight / 2

        val radius = mWidth.toFloat() / 2f
        setupText(cX, cY, radius.toInt())
        calculateLines(radius)
    }

    @ColorInt
    private fun bgColor(): Int = palette?.colorPrimary ?: Color.BLACK

    @ColorInt
    private fun northColor(): Int = palette?.colorSecondary ?: Color.RED

    @ColorInt
    private fun currentColor(): Int = palette?.colorSecondarySolid ?: Color.MAGENTA

    @ColorInt
    private fun dotColor(): Int = palette?.colorOnPrimary ?: Color.WHITE

    private fun calculateLines(minRadius: Float) {
        angleMap.clear()
        var angle = 0f
        while (angle < 360f) {
            angleMap[angle] = lineCoordinates(angle, minRadius)
            angle += 30f
        }
    }

    private fun lineCoordinates(angle: Float, radius: Float): AngledLine {
        val x1 = cX.toFloat() + radius * cos(Math.toRadians(angle.toDouble()))
        val y1 = cY.toFloat() + radius * sin(Math.toRadians(angle.toDouble()))

        val roundAngle = try {
            String.format(Locale.US, "%.2f", angle).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
        var length = if (roundAngle.toInt() % 90 == 0) {
            mLongLineLength
        } else {
            mShortLineLength
        }
        if (roundAngle == 0f) {
            length = mShortLineLength

            val xL = x1 + length * cos(Math.toRadians(225.0))
            val yL = y1 + length * sin(Math.toRadians(225.0))

            val xR = x1 + length * cos(Math.toRadians(135.0))
            val yR = y1 + length * sin(Math.toRadians(135.0))

            arrow = Arrow(PointF(x1.toFloat(), y1.toFloat()), PointF(xL.toFloat(), yL.toFloat()), PointF(xR.toFloat(), yR.toFloat()))
        }
        val x2 = cX.toFloat() + (radius - length) * cos(Math.toRadians(angle.toDouble()))
        val y2 = cY.toFloat() + (radius - length) * sin(Math.toRadians(angle.toDouble()))
        return AngledLine(
            angle,
            PointF(x1.toFloat(), y1.toFloat()),
            PointF(x2.toFloat(), y2.toFloat()),
            roundAngle == 0f
        )
    }

    data class Arrow(
        var start: PointF = PointF(0.0f, 0.0f),
        var left: PointF = PointF(0.0f, 0.0f),
        var right: PointF = PointF(0.0f, 0.0f))

    data class AngledLine(var angle: Float, var start: PointF = PointF(0.0f, 0.0f),
                          var end: PointF = PointF(0.0f, 0.0f), var isAnchor: Boolean = false
    )
}