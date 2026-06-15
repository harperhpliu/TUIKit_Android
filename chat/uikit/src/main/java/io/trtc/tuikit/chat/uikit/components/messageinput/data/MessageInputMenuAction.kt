package io.trtc.tuikit.chat.uikit.components.messageinput.data
import android.content.Context

data class MessageInputMenuAction(
    var title: String = "",
    var iconResID: Int = 0,
    var dangerous: Boolean = false,
    var onClick: () -> Unit = {},
    var order: Int = 1000
)

internal fun mergeMessageInputMenuActions(
    defaultActions: List<MessageInputMenuAction>,
    customActions: List<MessageInputMenuAction>
): List<MessageInputMenuAction> {
    return (defaultActions + customActions).sortedBy { it.order }
}

data class MessageInputMenuActionContext(
    val context: Context,
    val conversationID: String
)

fun interface MessageInputMenuActionProvider {
    fun getActions(context: MessageInputMenuActionContext): List<MessageInputMenuAction>
}
