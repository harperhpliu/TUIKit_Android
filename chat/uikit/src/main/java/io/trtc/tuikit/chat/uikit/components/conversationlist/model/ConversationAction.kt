package io.trtc.tuikit.chat.uikit.components.conversationlist.model
import android.content.Context
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo

data class ConversationMenuAction(
    var titleResID: Int = 0,
    var dangerous: Boolean = false,
    var action: (ConversationInfo) -> Unit = {},
    var order: Int = 1000
) {
    constructor(
        titleResID: Int,
        dangerous: Boolean,
        action: (ConversationInfo) -> Unit
    ) : this(
        titleResID = titleResID,
        dangerous = dangerous,
        action = action,
        order = 1000
    )
}

data class ConversationCustomAction(
    val title: String,
    val action: (ConversationInfo) -> Unit,
    val order: Int = 1000,
    val dangerousAction: Boolean = false
) {
    constructor(
        title: String,
        action: (ConversationInfo) -> Unit
    ) : this(
        title = title,
        action = action,
        order = 1000,
        dangerousAction = false
    )
}

data class ConversationCustomActionContext(
    val context: Context,
    val conversation: ConversationInfo
)

fun interface ConversationCustomActionProvider {
    fun getActions(context: ConversationCustomActionContext): List<ConversationCustomAction>
}

internal data class ConversationLongPressAction(
    val titleResID: Int = 0,
    val title: String = "",
    val dangerous: Boolean,
    val order: Int,
    val action: (ConversationInfo) -> Unit
)

internal fun mergeConversationLongPressActions(
    defaultActions: List<ConversationMenuAction>,
    customActions: List<ConversationCustomAction>
): List<ConversationLongPressAction> {
    val defaultActionItems = defaultActions.map {
        ConversationLongPressAction(
            titleResID = it.titleResID,
            dangerous = it.dangerous,
            order = it.order,
            action = it.action
        )
    }
    val customActionItems = customActions.map {
        ConversationLongPressAction(
            title = it.title,
            dangerous = it.dangerousAction,
            order = it.order,
            action = it.action
        )
    }
    return (defaultActionItems + customActionItems).sortedBy { it.order }
}
