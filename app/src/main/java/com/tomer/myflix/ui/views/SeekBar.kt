package com.tomer.myflix.ui.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withScale
import kotlin.math.roundToInt

class SeekBar : View {

    //region :: INIT-->>>
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(
        context,
        attr,
        defStyle
    )

    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    //endregion :: INIT-->>>

    //region :: LAYOUT

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val top = (desiredHeight - seekHeight) shr 1
        val start = dp10
        val end = w - start
        bgRect.set(start, top.toFloat(), end, (top + seekHeight).toFloat())
        progRect.set(start, top.toFloat(), end, (top + seekHeight).toFloat())
        secondaryProgRect.set(start, top.toFloat(), end, (top + seekHeight).toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        // Set the measured dimensions
        setMeasuredDimension(widthMeasureSpec, height)
    }

    //endregion :: LAYOUT

    //region :: GLOBALS-->>>

    var progress = .1f
        set(value) {
            field = value
            postInvalidate()
        }
    var secondaryProgress = .3f
    private var desiredHeight = 20.px().roundToInt()
    private var seekHeight = 6.px().roundToInt()
    private var maxRadius = 8.px()
    private var dp10 = 10.px()

    private val bgRect = RectF(0f, 0f, 0f, 0f)
    private val progRect = RectF(0f, 0f, 0f, 0f)
    private val secondaryProgRect = RectF(0f, 0f, 0f, 0f)

    private val paint = Paint().apply {
        color = Color.GRAY
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private var progColor = Color.RED
    private var bufferColor = Color.valueOf(1f, 0f, 0f, .4f).toArgb()
    private var handelRadius = maxRadius
    private var handelX = 0f

    private var seekLis: OnSeekChanged? = null

    //endregion :: GLOBALS-->>>

    //region :: DRAW

    override fun onDraw(canvas: Canvas) {
        canvas.withScale(1f, scaleY, 0f, bgRect.centerY()) {
            paint.color = Color.GRAY
            canvas.drawRoundRect(bgRect, 100f, 100f, paint)

            paint.color = bufferColor
            secondaryProgRect.right = (bgRect.width().times(secondaryProgress)) + bgRect.left
            canvas.drawRoundRect(secondaryProgRect, 100f, 100f, paint)

            paint.color = progColor
            progRect.right = (bgRect.width().times(progress)) + progRect.left
            if (!isTouching) handelX = progRect.right
            canvas.drawRoundRect(progRect, 100f, 100f, paint)
        }
        canvas.drawCircle(handelX, progRect.centerY(), handelRadius, paint)
    }

    //endregion DRAW

    private fun Number.px() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    )

    //region SEEK LIS

    private var scaleY = 1f
    private var showAnim = ValueAnimator.ofFloat(0f, 1f)
    private var hideAnim = ValueAnimator.ofFloat(0f, 1f)

    private var canTouch = true

    fun showAnim() {
        canTouch = true
        if (hideAnim.isRunning) hideAnim.cancel()
        showAnim = ValueAnimator.ofFloat(scaleY, 1f).apply {
            addUpdateListener {
                scaleY = it.animatedValue as Float
                handelRadius = scaleY.times(maxRadius)
                postInvalidate()
            }
            interpolator = OvershootInterpolator(1.6f)
            start()
        }
    }

    fun hideAnim() {
        canTouch = false
        if (showAnim.isRunning) showAnim.cancel()
        hideAnim = ValueAnimator.ofFloat(scaleY, 0f).apply {
            addUpdateListener {
                scaleY = it.animatedValue as Float
                handelRadius = scaleY.times(maxRadius)
                postInvalidate()
            }
            interpolator = AccelerateInterpolator(1.4f)
            start()
        }
    }

    interface OnSeekChanged {
        fun onStartTrackingTouch()
        fun onStopTrackingTouch(finalProg: Float)
    }

    fun setOnSeekBarChangeListener(lis: OnSeekChanged) {
        this.seekLis = lis
    }

    //endregion SEEK LIS

    private val stepUpAnim = ValueAnimator.ofFloat(maxRadius, dp10).apply {
        addUpdateListener {
            handelRadius = it.animatedValue as Float
            postInvalidate()
        }
    }

    private var stepDownAnim = ValueAnimator.ofFloat(handelRadius, maxRadius)

    private var touchX = 0f
    private var isTouching = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canTouch) return true
        if (event.action == MotionEvent.ACTION_DOWN) {
            isTouching = true
            touchX = event.x
            seekLis?.onStartTrackingTouch()

            if (stepDownAnim.isRunning) stepDownAnim.cancel()
            stepUpAnim.start()

        } else if (event.action == MotionEvent.ACTION_UP) {


            if (stepUpAnim.isRunning) stepUpAnim.cancel()
            stepDownAnim = ValueAnimator.ofFloat(handelRadius, maxRadius).apply {
                addUpdateListener {
                    handelRadius = it.animatedValue as Float
                    postInvalidate()
                }
                doOnEnd { isTouching = false }
                start()
            }

            val oldProg = progress
            val newProgress = (handelX - bgRect.left).div(bgRect.width()).coerceIn(0f, 1f)
            seekLis?.onStopTrackingTouch(newProgress)
            ValueAnimator.ofFloat(oldProg, newProgress).apply {
                addUpdateListener {
                    progress = it.animatedValue as Float
                }
                start()
            }

            postInvalidate()
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            handelX = (handelX + event.x - touchX).coerceIn(bgRect.left, bgRect.right)
            touchX = event.x
            postInvalidate()
        }

        return true
    }
}