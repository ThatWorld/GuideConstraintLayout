package io.github.thatworld.widget

import android.content.Context
import android.graphics.Path
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
    private var mHighlightPadding = 4f

    private var mHighlightPath: Path? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.GuideHighlightView) {
            mHighlightRadius = getDimension(R.styleable.GuideHighlightView_highlight_radius, mHighlightRadius)
            mHighlightPadding = getDimension(R.styleable.GuideHighlightView_highlight_padding, mHighlightPadding)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minSize = (maxOf(mHighlightRadius, mHighlightPadding) * 2).toInt()
        val width = resolveSize(minSize, widthMeasureSpec)
        val height = resolveSize(minSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    val highlightRadius
        get() = mHighlightRadius

    val highlightPadding
        get() = mHighlightPadding

    val highlightPath: Path?
        get() = mHighlightPath

    /**
     * 设置高亮区域的半径, 以像素为单位.
     */
    fun highlightRadius(radius: Float) {
        mHighlightRadius = radius
        invalidate()
    }

    /**
     * 设置高亮区域的内边距, 以像素为单位.
     *
     * @param padding 内边距大小
     */
    fun highlightPadding(padding: Float) {
        mHighlightPadding = padding
        invalidate()
    }

    /**
     * 设置自定义路径.
     *
     * @param path 自定义高亮路径
     */
    fun highlightPath(path: Path) {
        mHighlightPath = path
        invalidate()
    }
}