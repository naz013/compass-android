package com.github.naz013.compassapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.github.naz013.compassapp.R
import com.github.naz013.compassapp.theming.Palette

open class BaseCompassView : View {

    private val textPaint = Paint()
    private val angleLabelPainter = AngleLabelPainter(textPaint)

    var palette: Palette? = null
        set(value) {
            field = value
            if (value != null) {
                updateColors(value)
            }
            invalidate()
        }

    var degrees = 0.0f
        set(value) {
            field = value
            angleLabelPainter.degree = value
            onDegreeSet(value)
            invalidate()
        }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
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
            angleLabelPainter.draw(canvas)
        }
    }

    open fun onDegreeSet(value: Float) {

    }

    open fun updateColors(palette: Palette) {
        angleLabelPainter.setPalette(palette)
    }

    protected fun setupText(cX: Int, cY: Int, minInt: Int) {
        angleLabelPainter.bounds.left = cX - minInt
        angleLabelPainter.bounds.top = cY - minInt
        angleLabelPainter.bounds.right = cX + minInt
        angleLabelPainter.bounds.bottom = cY + minInt
        angleLabelPainter.updateBounds()
    }
}