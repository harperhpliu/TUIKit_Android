package io.trtc.tuikit.chat.uikit.components.messagelist.model
import android.content.Context
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

class MessageCustomAction(
    val title: String,
    val iconResID: Int,
    val action: (MessageInfo) -> Unit,
    val order: Int = 1000,
    val dangerousAction: Boolean = false
) {
    constructor(
        title: String,
        iconResID: Int,
        action: (MessageInfo) -> Unit
    ) : this(
        title = title,
        iconResID = iconResID,
        action = action,
        order = 1000,
        dangerousAction = false
    )
}

data class MessageCustomActionContext(
    val context: Context,
    val conversationID: String,
    val message: MessageInfo
)

fun interface MessageCustomActionProvider {
    fun getActions(context: MessageCustomActionContext): List<MessageCustomAction>
}

internal data class MessageLongPressAction(
    val title: String,
    val dangerousAction: Boolean,
    val iconResID: Int,
    val order: Int,
    val action: (MessageInfo) -> Unit
)

internal fun mergeMessageLongPressActions(
    defaultActions: List<MessageUIAction>,
    customActions: List<MessageCustomAction>
): List<MessageLongPressAction> {
    val defaultActionItems = defaultActions.map {
        MessageLongPressAction(
            title = it.name,
            dangerousAction = it.dangerousAction,
            iconResID = it.icon,
            order = it.order,
            action = it.action
        )
    }
    val customActionItems = customActions.map {
        MessageLongPressAction(
            title = it.title,
            dangerousAction = it.dangerousAction,
            iconResID = it.iconResID,
            order = it.order,
            action = it.action
        )
    }
    return (defaultActionItems + customActionItems).sortedBy { it.order }
}
