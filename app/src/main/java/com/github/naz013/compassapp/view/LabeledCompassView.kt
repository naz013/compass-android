package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.github.naz013.compassapp.theming.Palette
import com.github.naz013.compassapp.utils.ViewUtils
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class LabeledCompassView : BaseCompassView {

    private val paint = Paint()
    private var angleMap: MutableMap<Float, AngledDot> = mutableMapOf()
    private var arrow: Arrow? = null

    private var textBounds = Rect()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mShortLineLength: Float = 20f
    private var mLongLineLength: Float = 50f
    private var mNorthRadius: Float = 10f
    private var mRadius: Float = 10f
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
        mNorthRadius = ViewUtils.dp2px(context, 4).toFloat()

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20F, context.resources.displayMetrics)
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
            val list = mutableListOf<AngledDot>()

            canvas.save()
            canvas.rotate(rotationAngle, cX.toFloat(), cY.toFloat())
            angleMap.values.forEach { al ->
                if (al.isAnchor) {
                    paint.color = northColor
                    arrow?.let { ar ->
                        canvas.drawPath(ar.path, paint)
                    }
                    list.add(al)
                } else {
                    paint.color = lineColor
                    if (al.label.isEmpty()) {
                        canvas.drawCircle(al.start.x, al.start.y, mNorthRadius, paint)
                    } else {
                        list.add(al)
                    }
                }
            }
            canvas.restore()

            paint.color = lineColor
            list.forEach { ad ->
                paint.getTextBounds(ad.label, 0, ad.label.length, textBounds)

                val angle = ad.angle - degrees - 90
                val x = cX.toFloat() + mRadius * cos(Math.toRadians(angle.toDouble()))
                val y = cY.toFloat() + mRadius * sin(Math.toRadians(angle.toDouble()))

                canvas.drawText(ad.label, x.toFloat() - (textBounds.width() / 2), y.toFloat() + (textBounds.height() / 2), paint)
            }

            paint.color = currentColor
            canvas.drawLine(cX.toFloat(), 0f, cX.toFloat(), mLongLineLength * 3f, paint)
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)

        cX = mWidth / 2
        cY = mHeight / 2

        mRadius = mWidth.toFloat() / 2f - (mShortLineLength * 2)
        setupText(cX, cY, mRadius.toInt())
        calculateLines(mRadius)
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
            angle += 15f
        }
    }

    private fun lineCoordinates(angle: Float, radius: Float): AngledDot {
        val roundAngle = try {
            String.format(Locale.US, "%.2f", angle).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
        if (roundAngle == 0f) {
            val x1 = cX.toFloat() + (radius - (mShortLineLength * 2)) * cos(Math.toRadians(angle.toDouble()))
            val y1 = cY.toFloat() + (radius - (mShortLineLength * 2)) * sin(Math.toRadians(angle.toDouble()))

            val xL = x1 + mShortLineLength * cos(Math.toRadians(225.0))
            val yL = y1 + mShortLineLength * sin(Math.toRadians(225.0))

            val xR = x1 + mShortLineLength * cos(Math.toRadians(135.0))
            val yR = y1 + mShortLineLength * sin(Math.toRadians(135.0))

            val path = Path()
            path.moveTo(x1.toFloat(), y1.toFloat())
            path.lineTo(xL.toFloat(), yL.toFloat())
            path.lineTo(xR.toFloat(), yR.toFloat())
            path.close()

            arrow = Arrow(path)
        }
        val x1 = cX.toFloat() + radius * cos(Math.toRadians(angle.toDouble()))
        val y1 = cY.toFloat() + radius * sin(Math.toRadians(angle.toDouble()))
        return AngledDot(angle, PointF(x1.toFloat(), y1.toFloat()), newLabel(roundAngle), roundAngle == 0f)
    }

    private fun newLabel(angle: Float): String {
        return when {
            angle == 0f -> "N"
            angle == 90f -> "E"
            angle == 180f -> "S"
            angle == 270f -> "W"
            angle.toInt() % 10 == 0 -> "${angle.toInt()}"
            else -> ""
        }
    }

    data class Arrow(var path: Path)

    data class AngledDot(var angle: Float, var start: PointF = PointF(0.0f, 0.0f), var label: String = "",
                         var isAnchor: Boolean = false)
}