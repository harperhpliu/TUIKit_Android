package io.trtc.tuikit.chat.uikit.components.messagelist.ui.popups
import io.trtc.tuikit.chat.uikit.components.messagelist.model.MessageUIAction
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo

internal fun List<MessageUIAction>.withDeleteConfirmation(
    deleteActionName: String,
    onDeleteRequested: (MessageInfo, onConfirm: () -> Unit) -> Unit
): List<MessageUIAction> {
    return map { action ->
        if (action.name == deleteActionName && action.dangerousAction) {
            action.copy(
                action = { message ->
                    onDeleteRequested(message) {
                        action.action(message)
                    }
                }
            )
        } else {
            action
        }
    }
}
