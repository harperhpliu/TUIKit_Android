package io.trtc.tuikit.chat.uikit.pages
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.trtc.tuikit.chat.uikit.components.messageinput.config.ChatMessageInputConfig
import io.trtc.tuikit.chat.uikit.components.messageinput.config.MessageInputConfigProtocol
import io.trtc.tuikit.chat.uikit.components.messageinput.ui.MessageInputView
import io.trtc.tuikit.chat.uikit.components.messagelist.config.ChatMessageListConfig
import io.trtc.tuikit.chat.uikit.components.messagelist.config.MessageListConfigProtocol
import io.trtc.tuikit.chat.uikit.components.messagelist.ui.MessageListView
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import io.trtc.tuikit.chat.uikit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val messageListView: MessageListView
    private val messageInputView: MessageInputView

    private val themeStore = ThemeStore.shared(context)
    private var viewScope: CoroutineScope? = null

    private var onMultiSelectStateChanged: (Boolean) -> Unit = {}
    private var onUserClick: (String) -> Unit = {}

    init {
        LayoutInflater.from(context).inflate(R.layout.uikit_page_chat, this, true)
        messageListView = findViewById(R.id.uikit_message_list_view)
        messageInputView = findViewById(R.id.uikit_message_input_view)
        applyColors(themeStore.themeState.value.currentTheme.tokens.color)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        viewScope?.launch {
            themeStore.themeState.collectLatest { state ->
                applyColors(state.currentTheme.tokens.color)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope?.cancel()
        viewScope = null
    }

    private fun applyColors(colors: ColorTokens) {
        setBackgroundColor(colors.bgColorOperate)
    }

    fun setup(
        conversationID: String,
        locateMessage: MessageInfo? = null,
        messageListConfig: MessageListConfigProtocol = ChatMessageListConfig(),
        messageInputConfig: MessageInputConfigProtocol = ChatMessageInputConfig(),
        onMultiSelectStateChanged: (Boolean) -> Unit = {},
        onUserClick: (String) -> Unit = {}
    ) {
        this.onMultiSelectStateChanged = onMultiSelectStateChanged
        this.onUserClick = onUserClick
        messageListView.setup(
            conversationID = conversationID,
            config = messageListConfig,
            locateMessage = locateMessage,
            onMultiSelectStateChanged = { isMultiSelect ->
                messageInputView.visibility = if (isMultiSelect) GONE else VISIBLE
                this.onMultiSelectStateChanged(isMultiSelect)
            },
            onUserClick = { userID ->
                this.onUserClick(userID)
            }
        )
        messageInputView.setup(
            conversationID = conversationID,
            config = messageInputConfig
        )
    }

    fun exitMultiSelectMode() {
        messageListView.exitMultiSelectMode()
    }

    fun isInMultiSelectMode(): Boolean {
        return messageListView.isInMultiSelectMode()
    }

    fun release() {
        viewScope?.cancel()
        viewScope = null
        messageListView.release()
    }
}
