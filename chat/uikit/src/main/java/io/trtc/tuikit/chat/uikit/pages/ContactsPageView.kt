package io.trtc.tuikit.chat.uikit.pages
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import io.trtc.tuikit.chat.uikit.components.contactlist.ui.ContactListView
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsPageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val pageHeader: PageHeaderView
    private val contactListView: ContactListView

    private val themeStore = ThemeStore.shared(context)
    private var viewScope: CoroutineScope? = null

    private var onContactClick: ((ContactInfo) -> Unit)? = null
    private var onGroupClick: ((ContactInfo) -> Unit)? = null
    var onAddFriendClick: (() -> Unit)? = null
    var onAddGroupClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        layoutDirection = LAYOUT_DIRECTION_LOCALE

        pageHeader = PageHeaderView(context)
        addView(pageHeader, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        contactListView = ContactListView(context)
        addView(contactListView, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

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

    fun setHeaderTitle(title: String) {
        pageHeader.setTitle(title)
    }

    fun setHeaderRightAction(view: View) {
        pageHeader.setEditContent(view)
    }

    fun setup(
        onContactClick: ((ContactInfo) -> Unit)? = null,
        onGroupClick: ((ContactInfo) -> Unit)? = null
    ) {
        this.onContactClick = onContactClick
        this.onGroupClick = onGroupClick
        contactListView.setup(
            onContactClick = { contactInfo ->
                this.onContactClick?.invoke(contactInfo)
            },
            onGroupClick = { contactInfo ->
                this.onGroupClick?.invoke(contactInfo)
            }
        )
    }
}
