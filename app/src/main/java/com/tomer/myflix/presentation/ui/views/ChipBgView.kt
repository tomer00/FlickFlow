package com.tomer.myflix.presentation.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

class ChipBgView : View {

    //region :: INIT-->>>
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet, defStyle: Int) :
            super(context, attr, defStyle)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    //endregion :: INIT-->>>

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private val paintBg = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
    }

    private var roundCorners = 1f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        roundCorners = h.div(.5f)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            roundCorners,
            roundCorners,
            paintBg
        )
    }
    fun setAccentColor(@ColorInt col: Int) = run {
        paintBg.color = col
        postInvalidate()
    }
}