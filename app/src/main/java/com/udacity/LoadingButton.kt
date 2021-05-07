package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 48f
    }

    private lateinit var rect: Rect

    private var label = ""
    private var rectColor = 0
    private var animRectWidth = 0f
    private var animRectColor = 0
    private var arcColor = 0
    private var arcAngle = 0f
    private var textColor = 0

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        // If new button state is loading then start calculating animations
        if (new == ButtonState.Loading) {
            calculateAnimations(rect)
        }
    }


    init {
        isClickable = true

        rectColor = getColor(context, R.color.colorPrimary)
        animRectColor = getColor(context, R.color.colorPrimaryDark)
        arcColor = getColor(context, R.color.colorAccent)
        textColor = Color.WHITE

        paint.color = rectColor
    }

    override fun performClick(): Boolean {
        super.performClick()
        buttonState = ButtonState.Loading
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect = Rect(0, 0, w, 200)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rect, paint)

        // If button state is loading, draw the new loading rect and arc
        // based on the values set from calculateAnimations
        if (buttonState == ButtonState.Loading) {
            paint.color = animRectColor
            canvas.drawRect(0f, 0f, animRectWidth, 200f, paint)

            val left = ((rect.width() / 4f) * 3f) - 40f
            val top = (rect.height() / 2f) - 40f

            paint.color = arcColor
            canvas.drawArc(
                left, top,
                left + 50f, top + 50f,
                0f,
                arcAngle,
                true,
                paint
            )
        }

        paint.color = textColor

        label = when (buttonState) {
            ButtonState.Completed -> "Download"
            ButtonState.Clicked -> "Clicked"
            ButtonState.Loading -> "We are loading"
        }

        canvas.drawText(label, rect.width() / 2f, rect.height() / 2f, paint)

        paint.color = rectColor
    }

    /**
     * Calculates the animations for the loading rect and arc
     * and stores them into variables for the next draw
     */
    private fun calculateAnimations(rect: Rect) {
        valueAnimator.setFloatValues(0f, rect.width().toFloat())
        valueAnimator.duration = 2000

        valueAnimator.addUpdateListener { va ->
            if (va.animatedFraction == 1f) {
                buttonState = ButtonState.Completed
                va.removeAllUpdateListeners()
            }

            animRectWidth = va.animatedFraction * rect.width()
            arcAngle = va.animatedFraction * 360f

            invalidate()
        }

        valueAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}