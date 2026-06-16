package io.trtc.tuikit.chat.uikit.components.conversationlist.utils
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStoreOwner

internal tailrec fun Context.findConversationListViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findConversationListViewModelStoreOwner()
        else -> null
    }
}
