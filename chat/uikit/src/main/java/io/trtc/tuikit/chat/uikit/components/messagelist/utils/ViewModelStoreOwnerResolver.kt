package io.trtc.tuikit.chat.uikit.components.messagelist.utils
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStoreOwner

internal tailrec fun Context.findMessageListViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findMessageListViewModelStoreOwner()
        else -> null
    }
}
