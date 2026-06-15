package io.trtc.tuikit.chat.uikit.components.chatsetting.utils
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStoreOwner

internal tailrec fun Context.findViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findViewModelStoreOwner()
        else -> null
    }
}
