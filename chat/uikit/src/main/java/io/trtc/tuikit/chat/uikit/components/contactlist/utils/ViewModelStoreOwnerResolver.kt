package io.trtc.tuikit.chat.uikit.components.contactlist.utils
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStoreOwner

internal tailrec fun Context.findContactListViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findContactListViewModelStoreOwner()
        else -> null
    }
}
