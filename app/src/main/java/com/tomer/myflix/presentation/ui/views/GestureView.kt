package com.tomer.myflix.presentation.ui.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import com.tomer.myflix.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class GestureView : View {


    //region :: INIT-->>>
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet, defStyle: Int) :
            super(context, attr, defStyle)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    //endregion :: INIT-->>>

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    //region :: GLOBALS

    private var gestureLis: VideoGestureListener? = null
    private var tintCol = Color.argb(.32f, 0f, 0f, 0f)

    private var colAnim = ValueAnimator.ofArgb(tintCol, tintCol)
    private var canScrollUp = true
    private var canScrollUpONCE = true

    private var doubleTapped = false

    private var vol = 10
    private var maxVol = 15
    private var bright = 1f

    private var posBright = 0f
    private var posVol = 0f

    private val dp8 = 8.px().roundToInt()
    private val dp12 = 12.px()

    private val drFull by lazy { ContextCompat.getDrawable(context, R.drawable.ic_bright_full) }
    private val drLow by lazy { ContextCompat.getDrawable(context, R.drawable.ic_bright_low) }

    private val drVolLow by lazy { ContextCompat.getDrawable(context, R.drawable.volume_down) }
    private val drVolOff by lazy { ContextCompat.getDrawable(context, R.drawable.volume_off) }
    private val drVolHigh by lazy { ContextCompat.getDrawable(context, R.drawable.volume_up) }

    private var isSkipping: Boolean? = null
    private var maxRadius = 0f

    private val pathForward = Path()
    private val pathBackward = Path()

    private val colWhiteZeroAlpha = Color.argb(0f, 1f, 1f, 1f)
    private var colPCircle = Color.argb(.2f, 1f, 1f, 1f)
    private var rippleColAnim = ValueAnimator.ofFloat(0f, 0f)
    private val queueRipple: Queue<RippleAnimator> = LinkedList()
    private fun onEndRipple(isLast: Boolean) {
        if (isLast) {
            rippleColAnim = ValueAnimator.ofArgb(colPCircle, colWhiteZeroAlpha).apply {
                doOnEnd {
                    isSkipping = null
                }
                addUpdateListener {
                    colPCircle = it.animatedValue as Int
                    postInvalidate()
                }
                interpolator = LinearInterpolator()
                startDelay = 60
                duration = 160
                start()
            }
        }
        queueRipple.remove()
    }

    private fun onUpdateRipple() {
        postInvalidate()
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!canScrollUp) return false

                if (canScrollUpONCE)
                    if (distanceX.absoluteValue > distanceY.absoluteValue) {
                        canScrollUp = false
                        canScrollUpONCE = false
                        return false
                    } else canScrollUpONCE = false

                if (controlHidingDelayJob.isActive) controlHidingDelayJob.cancel()
                if (alphaAnimator.isRunning) alphaAnimator.cancel()
                if ((e1?.x ?: 0f) > width.div(2)) {
                    posVol = (distanceY.times(1.24f) + posVol).coerceIn(0f, height.toFloat())
                    showVol = true

                    val intVolNew = posVol.div(height).times(maxVol).roundToInt()
                    if (intVolNew < vol) {
                        gestureLis?.onVolume(false)
                        vol = intVolNew
                    } else if (intVolNew > vol)
                        gestureLis?.onVolume(true).also { vol = intVolNew }

                    postInvalidate()
                } else {
                    posBright = (distanceY.times(1.24f) + posBright).coerceIn(0f, height.toFloat())
                    showBright = true

                    val newBright =
                        posBright.div(height)

                    bright = newBright
                    gestureLis?.onBrightness(bright)

                    postInvalidate()
                }
                paintAlphaBg.alpha = 255f.times(0.4f).roundToInt()
                paintBg.alpha = 255
                paintProg.alpha = 255
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (doubleTapped) {
                    doubleTapped = false
                    return true
                }
                gestureLis?.onSingleTap()
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                doubleTapped = true
                val isForward = e.x > width.div(2)
                gestureLis?.onDoubleTap(isForward)

                rippleColAnim.cancel()
                rippleColAnim =
                    ValueAnimator.ofArgb(colPCircle, Color.argb(.2f, 1f, 1f, 1f)).apply {
                        addUpdateListener {
                            colPCircle = it.animatedValue as Int
                            postInvalidate()
                        }
                        interpolator = LinearInterpolator()
                        duration = 120
                        start()
                    }

                if (queueRipple.isNotEmpty())
                    queueRipple.last().isLast = false

                queueRipple.offer(
                    RippleAnimator(
                        maxRadius,
                        PointF(e.x, e.y),
                        if (isForward) pathForward else pathBackward,
                        { onUpdateRipple() },
                        { onEndRipple(it) }
                    )
                )

                isSkipping = isForward
                postInvalidate()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                gestureLis?.onLongPress(true)
                isLongClicked = true
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                postInvalidate()
            }

        })

    //endregion :: GLOBALS

    //region DRAW


    private var progHeight = 0
    private var xVol = 0f
    private var xBright = 0f
    private var y = 0f

    private val rectVol = Rect()
    private val rectBright = Rect()
    private val rectAlphaBgVol = RectF()
    private val rectAlphaBgBright = RectF()


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progHeight = h shr 1
        xVol = w.times(.8f)
        xBright = w.times(.2f)
        y = height.minus(progHeight).div(2f) + (dp8 shl 1)

        maxRadius = h.toFloat()
        val halfH = h.times(.5f)

        pathForward.reset()
        pathForward.addCircle(
            w.toFloat(),
            halfH,
            maxRadius,
            Path.Direction.CW
        )

        pathBackward.reset()
        pathBackward.addCircle(
            0f,
            halfH,
            maxRadius,
            Path.Direction.CW
        )

        rectVol.set(
            (xVol - (dp8 shl 1)).toInt(),
            (y - dp8.times(6)).toInt(),
            (xVol + (dp8 shl 1)).toInt(),
            (y - (dp8 shl 1)).toInt()
        )

        rectBright.set(
            (xBright - (dp8 shl 1)).toInt(),
            (y - dp8.times(6)).toInt(),
            (xBright + (dp8 shl 1)).toInt(),
            (y - (dp8 shl 1)).toInt()
        )

        rectAlphaBgVol.set(
            rectVol.left - dp12,
            rectVol.top - dp12,
            rectVol.right + dp12,
            y + progHeight + dp12
        )

        rectAlphaBgBright.set(
            rectBright.left - dp12,
            rectBright.top - dp12,
            rectBright.right + dp12,
            y + progHeight + dp12
        )
    }

    private var showVol = false
    private var showBright = false

    private val paintBg = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        strokeWidth = dp8.toFloat()
        strokeCap = Paint.Cap.ROUND
    }
    private val paintProg = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = dp8.toFloat()
        strokeCap = Paint.Cap.ROUND
    }

    private val paintAlphaBg = Paint().apply {
        color = Color.argb(.4f, 0f, 0f, 0f)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(tintCol)

        isSkipping?.let { isForward ->
            canvas.withClip(if (isForward) pathForward else pathBackward) {
                drawColor(colPCircle)
            }
            queueRipple.forEach { rippleAnimator ->
                rippleAnimator.draw(canvas)
            }
        }

        if (showVol) {
            val vol = posVol.div(height).coerceIn(0f, 1f)

            canvas.drawRoundRect(rectAlphaBgVol, dp12, dp12, paintAlphaBg)
            canvas.drawLine(xVol, y, xVol, y + progHeight, paintBg)

            if (this.vol == 0) {
                drVolOff?.bounds = rectVol
                drVolOff?.draw(canvas)
            } else if (this.vol < maxVol.div(2f)) {
                drVolLow?.bounds = rectVol
                drVolLow?.draw(canvas)
                canvas.drawLine(
                    xVol,
                    y + (progHeight.times(1 - vol)),
                    xVol,
                    y + progHeight,
                    paintProg
                )
            } else {
                drVolHigh?.bounds = rectVol
                drVolHigh?.draw(canvas)
                canvas.drawLine(
                    xVol,
                    y + (progHeight.times(1 - vol)),
                    xVol,
                    y + progHeight,
                    paintProg
                )
            }
        }
        if (showBright) {
            canvas.drawRoundRect(rectAlphaBgBright, dp12, dp12, paintAlphaBg)
            canvas.drawLine(xBright, y, xBright, y + progHeight, paintBg)
            canvas.drawLine(
                xBright,
                y + (progHeight.times(1 - bright)),
                xBright,
                y + progHeight,
                paintProg
            )

            if (bright < .5f) {
                drLow?.bounds = rectBright
                canvas.withRotation(
                    bright.times(360),
                    drLow?.bounds?.exactCenterX() ?: 0f,
                    drLow?.bounds?.exactCenterY() ?: 0f
                ) {
                    drLow?.draw(this)
                }
            } else {
                drFull?.bounds = rectBright
                canvas.withRotation(
                    bright.times(360),
                    drFull?.bounds?.exactCenterX() ?: 0f,
                    drFull?.bounds?.exactCenterY() ?: 0f
                ) {
                    drFull?.draw(this)
                }
            }

        }
    }
    //endregion DRAW

    private var isLongClicked = false
    private var controlHidingDelayJob = CoroutineScope(Dispatchers.Main).launch { delay(100) }
    private var alphaAnimator = ValueAnimator.ofInt(0, 0)

    @SuppressLint("ClickableViewAccessibility", "InlinedApi")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            if (isLongClicked) {
                isLongClicked = false
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
                gestureLis?.onLongPress(false)
            }
            if (showVol) {
                if (controlHidingDelayJob.isActive) controlHidingDelayJob.cancel()
                controlHidingDelayJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(100)
                    alphaAnimator = ValueAnimator.ofInt(255, 0).apply {
                        doOnEnd {
                            showVol = false
                            postInvalidate()
                        }

                        this.addUpdateListener {
                            paintAlphaBg.alpha = (it.animatedValue as Int).times(.4f).roundToInt()
                            paintBg.alpha = it.animatedValue as Int
                            paintProg.alpha = it.animatedValue as Int
                            postInvalidate()
                        }
                        duration = 200
                        start()
                    }
                }
            }
            if (showBright) {
                if (controlHidingDelayJob.isActive) controlHidingDelayJob.cancel()
                controlHidingDelayJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(100)
                    alphaAnimator = ValueAnimator.ofInt(255, 0).apply {
                        doOnEnd {
                            showBright = false
                            postInvalidate()
                        }

                        this.addUpdateListener {
                            paintAlphaBg.alpha = (it.animatedValue as Int).times(.4f).roundToInt()
                            paintBg.alpha = it.animatedValue as Int
                            paintProg.alpha = it.animatedValue as Int
                            postInvalidate()
                        }
                        duration = 200
                        start()
                    }
                }
            }
            if (!canScrollUp)
                canScrollUp = true
            if (!canScrollUpONCE)
                canScrollUpONCE = true
            postInvalidate()
        } else if (event.action == MotionEvent.ACTION_DOWN) gestureLis?.requestVolSync()
        gestureDetector.onTouchEvent(event)
        return true
    }

    //region COMMUNICATION

    fun setVideoGestureLis(lis: VideoGestureListener) = run { this.gestureLis = lis }
    fun setVisi(isVisible: Boolean) {
        if (isVisible) {
            if (colAnim.isRunning) colAnim.cancel()
            colAnim = ValueAnimator.ofArgb(tintCol, Color.argb(.32f, 0f, 0f, 0f)).apply {
                addUpdateListener {
                    tintCol = it.animatedValue as Int
                    invalidate()
                }
                start()
            }
            return
        }

        if (colAnim.isRunning) colAnim.cancel()
        colAnim = ValueAnimator.ofArgb(tintCol, Color.argb(0f, 0f, 0f, 0f)).apply {
            addUpdateListener {
                tintCol = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    fun setVolAndBrightNess(vol: Int, maxVol: Int, bright: Float) {
        this.vol = vol
        this.maxVol = maxVol
        this.bright = bright

        this.posVol = height.div(maxVol).times(vol).toFloat()
        this.posBright = height.times(bright)
    }

    fun setAccentColor(@ColorInt color: Int) {
        paintProg.color = color
        postInvalidate()
    }

    //endregion COMMUNICATION

    private fun Number.px() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    )

    interface VideoGestureListener {
        fun onSingleTap()
        fun onDoubleTap(isForward: Boolean)
        fun onVolume(increase: Boolean)
        fun onBrightness(bright: Float)
        fun onLongPress(isDown: Boolean)

        fun requestVolSync()
    }

    private class RippleAnimator(
        private val maxRadius: Float,
        private val center: PointF,
        private val path: Path,
        onUpdate: () -> Unit,
        onEnd: (Boolean) -> Unit
    ) {
        var isLast = true
        private var radius = 0f
        private val paintRippleBlur = Paint().apply {
            color = Color.argb(.2f, 1f, 1f, 1f)
            maskFilter = BlurMaskFilter(40.px(), BlurMaskFilter.Blur.NORMAL)
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
        }

        init {
            ValueAnimator.ofFloat(0f, maxRadius.times(1.2f)).apply {
                addUpdateListener {
                    radius = it.animatedValue as Float
                    onUpdate()
                }
                doOnEnd {
                    if (isLast) {
                        onEnd(true)
                        return@doOnEnd
                    }
                    ValueAnimator.ofArgb(paintRippleBlur.color, Color.argb(0f, 1f, 1f, 1f)).apply {
                        doOnEnd { onEnd(false) }
                        addUpdateListener {
                            paintRippleBlur.color = it.animatedValue as Int
                            onUpdate()
                        }
                        interpolator = LinearInterpolator()
                        duration = 180
                        start()
                    }
                }
                interpolator = AccelerateInterpolator()
                this.duration = 460
                start()
            }
        }

        fun draw(canvas: Canvas) {
            canvas.withClip(path) {
                canvas.drawCircle(center.x, center.y, radius, paintRippleBlur)
            }
        }

        private fun Number.px() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
        )
    }


}