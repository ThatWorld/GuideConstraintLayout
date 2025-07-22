package io.github.thatworld.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible

class GuideConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private var mHighlightBackgroundColor = 0x80000000.toInt() // 默认半透明黑色
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

    init {
        setWillNotDraw(false)
        context.withStyledAttributes(attrs, R.styleable.GuideConstraintLayout, defStyleAttr, defStyleRes) {
            mHighlightBackgroundColor = getColor(R.styleable.GuideConstraintLayout_highlight_background_color, mHighlightBackgroundColor)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // 创建离屏缓冲层
        val layer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), mPaint)

        // 绘制半透明背景
        canvas.drawColor(mHighlightBackgroundColor)

        // 设置挖空模式
        mPaint.xfermode = mXfermode

        // 挖空高亮区域（必须在背景之后绘制）
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (!child.isVisible) continue

            if (child is GuideHighlightView) {
                val path = child.highlightPath
                if (path != null) {
                    canvas.drawPath(path, mPaint)
                } else {
                    val radius = child.highlightRadius
                    val padding = child.highlightPadding
                    canvas.drawRoundRect(
                        child.left - padding,
                        child.top - padding,
                        child.right + padding,
                        child.bottom + padding,
                        radius,
                        radius,
                        mPaint
                    )
                }
            }
        }

        // 恢复混合模式
        mPaint.xfermode = null

        // 绘制子视图（在挖空区域上方显示）
        super.dispatchDraw(canvas)

        // 恢复画布
        canvas.restoreToCount(layer)
    }

    /**
     * 将某个高亮视图绑定到指定的矩形区域上，并保证 [ConstraintLayout] 原本跟随[highlight]的依赖关系跟随位置变化。
     *
     * @param rect 目标矩形区域，通常是需要高亮的区域。
     * @param highlight 高亮视图 [GuideHighlightView] 用于显示高亮效果。
     */
    fun bindTarget(rect: RectF, highlight: GuideHighlightView) {
        val radius = highlight.highlightRadius
        val padding = highlight.highlightPadding

        // 保留高亮路径逻辑
        highlight.highlightPath(Path().apply {
            addRoundRect(
                rect.left - padding,
                rect.top - padding,
                rect.right + padding,
                rect.bottom + padding,
                radius,
                radius,
                Path.Direction.CW
            )
        })

        refresh(rect, highlight)
    }

    /**
     * 将某个高亮视图绑定到指定的矩形区域上，并保证 [ConstraintLayout] 原本跟随[highlightId]的依赖关系跟随位置变化。
     *
     * @param rect 目标矩形区域，通常是需要高亮的区域。
     * @param highlightId 高亮视图的资源 ID。
     */
    fun bindTarget(rect: RectF, @IdRes highlightId: Int) {
        val highlightView = findViewById<GuideHighlightView>(highlightId)
        if (highlightView != null) {
            bindTarget(rect, highlightView)
        } else {
            throw IllegalArgumentException("Highlight view with ID $highlightId not found.")
        }
    }

    /**
     * 将某个高亮视图绑定到目标视图上，并保证 [ConstraintLayout] 原本跟随[highlight]的依赖关系跟随位置变化。
     *
     * @param target 目标视图，通常是需要高亮的按钮或其他 UI 元素。
     * @param highlight 高亮视图 [GuideHighlightView] 用于显示高亮效果。
     */
    fun bindTarget(target: View, highlight: GuideHighlightView) {
        // 获取目标视图位置
        val location = IntArray(2)
        target.getLocationInWindow(location)
        val parentLocation = IntArray(2)
        this.getLocationInWindow(parentLocation)
        val offsetX = (location[0] - parentLocation[0]).toFloat()
        val offsetY = (location[1] - parentLocation[1]).toFloat()

        bindTarget(
            RectF(
                offsetX,
                offsetY,
                offsetX + target.width,
                offsetY + target.height,
            ),
            highlight
        )
    }

    /**
     * 将某个高亮视图绑定到指定的矩形区域上，并保证 [ConstraintLayout] 原本跟随[highlightId]的依赖关系跟随位置变化。
     *
     * @param target 目标视图，通常是需要高亮的按钮或其他 UI 元素。
     * @param highlightId 高亮视图的资源 ID。
     */
    fun bindTarget(target: View, @IdRes highlightId: Int) {
        val highlightView = findViewById<GuideHighlightView>(highlightId)
        if (highlightView != null) {
            bindTarget(target, highlightView)
        } else {
            throw IllegalArgumentException("Highlight view with ID $highlightId not found.")
        }
    }

    /**
     * 刷新高亮视图的位置和大小。
     *
     * @param rect 目标矩形区域，通常是需要高亮的区域。
     * @param highlight 高亮视图 [GuideHighlightView] 用于显示高亮效果。
     */
    fun refresh(rect: RectF, highlight: GuideHighlightView) {
        val padding = highlight.highlightPadding

        // 创建并应用新约束
        val constraintSet = ConstraintSet()
        constraintSet.clone(this) // 克隆当前约束

        // 清除旧约束
        constraintSet.clear(highlight.id)

        // 添加新约束（定位到父布局 + 偏移量）
        constraintSet.connect(
            highlight.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            (rect.left + padding).toInt()
        )
        constraintSet.connect(
            highlight.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            (rect.top + padding).toInt()
        )

        // 固定宽高（与目标视图一致）
        constraintSet.constrainWidth(highlight.id, rect.width().toInt())
        constraintSet.constrainHeight(highlight.id, rect.height().toInt())

        // 应用约束
        constraintSet.applyTo(this)
    }

    /**
     * 刷新高亮视图的位置和大小。
     *
     * @param rect 目标矩形区域，通常是需要高亮的区域。
     * @param highlightId 高亮视图的资源 ID。
     */
    fun refresh(rect: RectF, @IdRes highlightId: Int) {
        val highlightView = findViewById<GuideHighlightView>(highlightId)
        if (highlightView != null) {
            refresh(rect, highlightView)
        } else {
            throw IllegalArgumentException("Highlight view with ID $highlightId not found.")
        }
    }

    /**
     * 刷新高亮视图的位置和大小。
     *
     * @param target 目标视图，通常是需要高亮的按钮或其他 UI 元素。
     * @param highlight 高亮视图 [GuideHighlightView] 用于显示高亮效果。
     */
    fun refresh(target: View, highlight: GuideHighlightView) {
        // 获取目标视图位置
        val location = IntArray(2)
        target.getLocationInWindow(location)
        val parentLocation = IntArray(2)
        this.getLocationInWindow(parentLocation)
        val offsetX = (location[0] - parentLocation[0]).toFloat()
        val offsetY = (location[1] - parentLocation[1]).toFloat()

        refresh(
            RectF(
                offsetX,
                offsetY,
                offsetX + target.width,
                offsetY + target.height,
            ),
            highlight
        )
    }

    /**
     * 刷新高亮视图的位置和大小。
     *
     * @param target 目标视图，通常是需要高亮的按钮或其他 UI 元素。
     * @param highlightId 高亮视图的资源 ID。
     */
    fun refresh(target: View, @IdRes highlightId: Int) {
        val highlightView = findViewById<GuideHighlightView>(highlightId)
        if (highlightView != null) {
            refresh(target, highlightView)
        } else {
            throw IllegalArgumentException("Highlight view with ID $highlightId not found.")
        }
    }
}