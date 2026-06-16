package io.trtc.tuikit.chat.uikit.components.conversationlist.model
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.atomicxcore.api.conversation.ConversationInfo
import io.trtc.tuikit.atomicxcore.api.conversation.ReceiveMessageOption

object ConversationMuteAction {
    fun create(
        conversation: ConversationInfo,
        onMuteConversation: (conversationID: String, mute: Boolean) -> Unit
    ): ConversationMenuAction {
        return create(
            conversation = conversation,
            order = 1000,
            onMuteConversation = onMuteConversation
        )
    }

    fun create(
        conversation: ConversationInfo,
        order: Int,
        onMuteConversation: (conversationID: String, mute: Boolean) -> Unit
    ): ConversationMenuAction {
        val mute = conversation.receiveOption == ReceiveMessageOption.RECEIVE
        return ConversationMenuAction(
            titleResID = if (mute) {
                R.string.conversation_list_mute
            } else {
                R.string.conversation_list_unmute
            },
            action = { target ->
                onMuteConversation(target.conversationID, mute)
            },
            order = order
        )
    }
}
