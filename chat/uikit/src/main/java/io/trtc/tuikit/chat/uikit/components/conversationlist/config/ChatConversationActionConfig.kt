package io.trtc.tuikit.chat.uikit.components.conversationlist.config
import io.trtc.tuikit.chat.uikit.components.config.AppBuilderConfig
import io.trtc.tuikit.chat.uikit.components.config.ConversationAction
import io.trtc.tuikit.chat.uikit.components.conversationlist.model.ConversationCustomActionProvider

interface ConversationActionConfigProtocol {
    val isSupportDelete: Boolean
    val isSupportMute: Boolean
    val isSupportPin: Boolean
    val isSupportMarkUnread: Boolean
    val isSupportClearHistory: Boolean
}

interface ConversationCustomActionConfigProtocol {
    val customActionProvider: ConversationCustomActionProvider?
}

class ChatConversationActionConfig(
    private var _isSupportDelete: Boolean? = null,
    private var _isSupportMute: Boolean? = null,
    private var _isSupportPin: Boolean? = null,
    private var _isSupportMarkUnread: Boolean? = null,
    private var _isSupportClearHistory: Boolean? = null
) : ConversationActionConfigProtocol, ConversationCustomActionConfigProtocol {

    private var _customActionProvider: ConversationCustomActionProvider? = null

    override var isSupportDelete: Boolean
        get() = _isSupportDelete
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.DELETE)
        set(value) {
            _isSupportDelete = value
        }

    override var isSupportMute: Boolean
        get() = _isSupportMute
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.MUTE)
        set(value) {
            _isSupportMute = value
        }

    override var isSupportPin: Boolean
        get() = _isSupportPin
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.PIN)
        set(value) {
            _isSupportPin = value
        }

    override var isSupportMarkUnread: Boolean
        get() = _isSupportMarkUnread
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.MARK_UNREAD)
        set(value) {
            _isSupportMarkUnread = value
        }

    override var isSupportClearHistory: Boolean
        get() = _isSupportClearHistory
            ?: AppBuilderConfig.conversationActionList.contains(ConversationAction.CLEAR_HISTORY)
        set(value) {
            _isSupportClearHistory = value
        }

    override val customActionProvider: ConversationCustomActionProvider?
        get() = _customActionProvider

    fun setCustomActionProvider(provider: ConversationCustomActionProvider?): ChatConversationActionConfig {
        _customActionProvider = provider
        return this
    }
}
