package io.trtc.tuikit.chat.uikit.components.messagelist.ui.layout
import androidx.recyclerview.widget.RecyclerView

internal object MessageListAutoScrollPolicy {
    fun shouldScrollForReceivedMessage(
        firstVisibleItem: Int,
        firstCompletelyVisibleItem: Int
    ): Boolean {
        if (firstVisibleItem == RecyclerView.NO_POSITION ||
            firstCompletelyVisibleItem == RecyclerView.NO_POSITION
        ) {
            return false
        }
        return firstVisibleItem == 0 && firstCompletelyVisibleItem == 0
    }

    fun shouldScrollForSentMessage(): Boolean = true
}
