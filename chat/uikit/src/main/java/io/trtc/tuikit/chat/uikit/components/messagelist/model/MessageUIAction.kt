package io.trtc.tuikit.chat.uikit.components.messagelist.model
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

data class MessageUIAction(
    var name: String,
    var dangerousAction: Boolean = false,
    var icon: Int = R.drawable.message_list_menu_more_icon,
    var action: (MessageInfo) -> Unit,
    var order: Int = 1000
) {
    constructor(
        name: String,
        dangerousAction: Boolean,
        icon: Int,
        action: (MessageInfo) -> Unit
    ) : this(
        name = name,
        dangerousAction = dangerousAction,
        icon = icon,
        action = action,
        order = 1000
    )
}
