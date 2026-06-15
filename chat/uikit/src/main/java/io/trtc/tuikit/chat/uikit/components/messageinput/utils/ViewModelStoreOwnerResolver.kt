package io.trtc.tuikit.chat.uikit.components.messageinput.utils
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStoreOwner

internal tailrec fun Context.findMessageInputViewModelStoreOwner(): ViewModelStoreOwner? {
    return when (this) {
        is ViewModelStoreOwner -> this
        is ContextWrapper -> baseContext.findMessageInputViewModelStoreOwner()
        else -> null
    }
}
