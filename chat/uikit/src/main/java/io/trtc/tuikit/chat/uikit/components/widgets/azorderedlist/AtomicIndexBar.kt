package io.trtc.tuikit.chat.uikit.components.widgets.azorderedlist
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.trtc.tuikit.atomicx.common.util.ScreenUtil.dp2px
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AtomicIndexBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val LETTER_WIDTH_DP = 24f
        private const val HIGHLIGHTED_SIZE_DP = 20f
        private const val NORMAL_SIZE_DP = 16f
        private const val LETTER_SPACING_DP = 1f
        private const val FONT_SIZE_SP = 10f
        private const val VERTICAL_PADDING_DP = 8f
    }

    private var letters: List<String> = emptyList()
    private var currentLetter: String? = null
    private var isDragging = false
    private var draggedLetter: String? = null

    var onLetterSelected: ((String) -> Unit)? = null
    var onDragStart: (() -> Unit)? = null
    var onDragEnd: (() -> Unit)? = null

    private var viewScope: CoroutineScope? = null

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setLetters(letters: List<String>) {
        this.letters = letters
        requestLayout()
        invalidate()
    }

    fun setCurrentLetter(letter: String?) {
        if (currentLetter != letter) {
            currentLetter = letter
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dm = resources.displayMetrics
        val widthPx = dp2px(LETTER_WIDTH_DP, dm).toInt()

        val verticalPaddingPx = dp2px(VERTICAL_PADDING_DP, dm)
        val spacingPx = dp2px(LETTER_SPACING_DP, dm)
        val slotHeightPx = dp2px(NORMAL_SIZE_DP, dm)
        val contentHeight = if (letters.isEmpty()) {
            0f
        } else {
            letters.size * slotHeightPx + (letters.size - 1) * spacingPx + verticalPaddingPx * 2
        }

        setMeasuredDimension(
            resolveSize(widthPx, widthMeasureSpec),
            resolveSize(contentHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (letters.isEmpty()) return

        val dm = resources.displayMetrics
        val colors = getColors()

        val highlightedSizePx = dp2px(HIGHLIGHTED_SIZE_DP, dm)
        val normalSizePx = dp2px(NORMAL_SIZE_DP, dm)
        val spacingPx = dp2px(LETTER_SPACING_DP, dm)
        val verticalPaddingPx = dp2px(VERTICAL_PADDING_DP, dm)
        val fontSizePx = dp2px(FONT_SIZE_SP, dm)
        val centerX = width / 2f

        var currentY = verticalPaddingPx

        letters.forEach { letter ->
            val isDraggedLetter = isDragging && draggedLetter == letter
            val isCurrent = !isDragging && currentLetter == letter
            val isHighlighted = isDraggedLetter || isCurrent

            val slotHeight = normalSizePx
            val centerY = currentY + slotHeight / 2

            if (isHighlighted) {
                bgPaint.color = colors.textColorLink
                canvas.drawCircle(centerX, centerY, highlightedSizePx / 2, bgPaint)
            }

            textPaint.textSize = fontSizePx
            textPaint.color = if (isHighlighted) colors.textColorButton else colors.textColorLink
            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(letter, centerX, textY, textPaint)

            currentY += slotHeight + spacingPx
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (letters.isEmpty()) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                onDragStart?.invoke()
                parent?.requestDisallowInterceptTouchEvent(true)
                val letter = getLetterFromY(event.y)
                letter?.let {
                    draggedLetter = it
                    onLetterSelected?.invoke(it)
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val letter = getLetterFromY(event.y)
                letter?.let {
                    if (draggedLetter != it) {
                        draggedLetter = it
                        onLetterSelected?.invoke(it)
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                draggedLetter = null
                onDragEnd?.invoke()
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getLetterFromY(y: Float): String? {
        if (letters.isEmpty() || height <= 0) return null
        val dm = resources.displayMetrics
        val verticalPaddingPx = dp2px(VERTICAL_PADDING_DP, dm)
        val spacingPx = dp2px(LETTER_SPACING_DP, dm)
        val slotHeightPx = dp2px(NORMAL_SIZE_DP, dm)
        val stepPx = slotHeightPx + spacingPx
        if (stepPx <= 0f) return null

        val relativeY = y - verticalPaddingPx
        if (relativeY <= 0f) return letters.first()

        val index = (relativeY / stepPx).toInt().coerceIn(0, letters.lastIndex)
        return letters[index]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        viewScope?.launch {
            ThemeStore.shared(context).themeState.collectLatest {
                invalidate()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        viewScope = null
    }

    private fun getColors(): ColorTokens {
        return ThemeStore.shared(context).themeState.value.currentTheme.tokens.color
    }

}
