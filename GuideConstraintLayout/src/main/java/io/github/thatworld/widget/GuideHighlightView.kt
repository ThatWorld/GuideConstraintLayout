package io.github.thatworld.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes

class GuideHighlightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private var mHighlightRadius = 12f
    private var mHighlightSpace = 4f

    init {
        context.withStyledAttributes(attrs, R.styleable.GuideHighlightView) {
            mHighlightRadius = getDimension(R.styleable.GuideHighlightView_highlight_radius, mHighlightRadius)
            mHighlightSpace = getDimension(R.styleable.GuideHighlightView_highlight_space, mHighlightSpace)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val minWidth = if (widthMode == MeasureSpec.AT_MOST)
            mHighlightSpace * 2 // left and right padding
        else
            MeasureSpec.getSize(widthMeasureSpec) + mHighlightSpace

        val minHeight = if (heightMode == MeasureSpec.AT_MOST)
            mHighlightSpace * 2 // top and bottom padding
        else
            MeasureSpec.getSize(heightMeasureSpec) + mHighlightSpace

        setMeasuredDimension(minWidth.toInt(), minHeight.toInt())
    }

    // Test preview
    // override fun onDraw(canvas: Canvas) {
    //     super.onDraw(canvas)
    //
    //     val left = 0f
    //     val top = 0f
    //     val right = width.toFloat()
    //     val bottom = height.toFloat()
    //
    //     canvas.drawRoundRect(left, top, right, bottom, mHighlightRadius, mHighlightRadius, mPaint)
    // }

    val highlightRadius
        get() = mHighlightRadius

    val highlightSpace
        get() = mHighlightSpace

    /**
     * 设置高亮区域的半径。
     */
    fun setHighlightRadius(radius: Float) {
        mHighlightRadius = radius
        invalidate()
    }

    /**
     * 设置高亮区域的内部空间距离。
     *
     * @param space 内部空间距离
     */
    fun setHighlightSpace(space: Float) {
        mHighlightSpace = space
        invalidate()
    }
}