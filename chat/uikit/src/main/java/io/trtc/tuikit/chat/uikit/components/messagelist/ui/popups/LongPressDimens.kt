package io.trtc.tuikit.chat.uikit.components.messagelist.ui.popups
import io.trtc.tuikit.chat.uikit.components.messagelist.ui.reactions.MessageReactionPanelPolicy
import kotlin.math.roundToInt

internal object LongPressDimens {
    const val COLUMNS = 4
    const val MAX_ROWS = 2
    const val PAGE_SIZE = COLUMNS * MAX_ROWS

    const val SWITCH_ANIMATION_DURATION = 220L

    const val PAGE_INDICATOR_INACTIVE_ALPHA = 64

    fun screenMargin(density: Float): Int = 8.dp(density)

    fun visualGap(density: Float): Int = 4.dp(density)

    fun pageIndicatorDotSize(density: Float): Int = 5.dp(density)

    fun pageIndicatorDotSpacing(density: Float): Int = 4.dp(density)

    fun pageIndicatorVerticalPadding(density: Float): Int = 6.dp(density)

    fun pageIndicatorAreaHeight(density: Float): Int {
        return pageIndicatorDotSize(density) + pageIndicatorVerticalPadding(density) * 2
    }

    fun popupItemCellWidth(density: Float): Int = 56.dp(density)

    fun popupItemCellHeight(density: Float): Int = 64.dp(density)

    fun popupPageVerticalPadding(density: Float): Int = 8.dp(density) * 2

    fun popupDividerHeight(density: Float): Int = 1 + 4.dp(density) * 2

    fun popupPageHeight(rowCount: Int, density: Float): Int {
        val safeRowCount = rowCount.coerceAtLeast(1)
        return popupPageVerticalPadding(density) +
            popupItemCellHeight(density) * safeRowCount +
            popupDividerHeight(density) * (safeRowCount - 1)
    }

    fun popupPagerVerticalPadding(): Int = 0

    fun resolveSinglePageColumnCount(itemCount: Int): Int {
        return itemCount.coerceIn(1, COLUMNS)
    }

    fun popupCardWidth(columnCount: Int, density: Float): Int {
        return popupItemCellWidth(density) * columnCount + 8.dp(density) * 2
    }

    fun cardWidthForColumns(
        columnCount: Int,
        density: Float,
        quickPickerRowWidth: Int
    ): Int {
        return if (columnCount >= COLUMNS) {
            maxOf(popupCardWidth(COLUMNS, density), quickPickerRowWidth)
        } else {
            popupCardWidth(columnCount, density)
        }
    }

    fun emojiPanelHorizontalPadding(density: Float): Int = 8.dp(density)

    fun emojiPanelVerticalPadding(density: Float): Int = 8.dp(density)

    fun emojiCellSize(density: Float): Int = 32.dp(density)

    fun emojiPanelPageHeight(density: Float): Int {
        return emojiCellSize(density) * MessageReactionPanelPolicy.ROWS
    }

    fun emojiPanelContentWidth(density: Float): Int {
        return emojiPanelHorizontalPadding(density) * 2 +
            emojiCellSize(density) * MessageReactionPanelPolicy.COLUMNS
    }

    private fun Int.dp(density: Float): Int = (this * density).roundToInt()
}
