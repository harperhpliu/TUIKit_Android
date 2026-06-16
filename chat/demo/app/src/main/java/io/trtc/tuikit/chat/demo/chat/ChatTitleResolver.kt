package io.trtc.tuikit.chat.demo.chat

internal object ChatTitleResolver {
    fun resolve(
        conversationTitle: String? = null,
        conversationID: String
    ): String {
        return conversationTitle.takeUnless { it.isNullOrBlank() }
            ?: conversationID
    }
}
