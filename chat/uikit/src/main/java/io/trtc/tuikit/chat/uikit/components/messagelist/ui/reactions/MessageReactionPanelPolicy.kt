package io.trtc.tuikit.chat.uikit.components.messagelist.ui.reactions
object MessageReactionPanelPolicy {
    const val COLUMNS = 7
    const val ROWS = 3
    const val PAGE_SIZE = COLUMNS * ROWS

    fun pageCount(emojiCount: Int): Int {
        if (emojiCount <= 0) {
            return 1
        }
        return ((emojiCount + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
    }
}
