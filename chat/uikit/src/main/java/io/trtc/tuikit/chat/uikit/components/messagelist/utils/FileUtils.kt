package io.trtc.tuikit.chat.uikit.components.messagelist.utils
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import io.trtc.tuikit.chat.uikit.R
import java.io.File
import java.util.Locale

object FileUtils {
    private const val TAG = "MessageListFileUtils"
    private const val FILE_PROVIDER_AUTH = ".MessageList.FileProvider"

    fun openFile(context: Context, path: String, fileName: String?) {
        val uri = getUriFromPath(context, path) ?: run {
            Log.e(TAG, "openFile failed, uri is null")
            return
        }
        val fileExtension = if (fileName.isNullOrEmpty()) {
            getFileExtensionFromUrl(path)
        } else {
            getFileExtensionFromUrl(fileName)
        }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, mimeType)
        }
        try {
            val chooserIntent = Intent.createChooser(
                intent,
                context.getString(R.string.message_list_open_file_tips)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "openFile failed, ${exception.message}", exception)
        }
    }

    private fun getUriFromPath(context: Context, path: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    context.applicationInfo.packageName + FILE_PROVIDER_AUTH,
                    File(path)
                )
            } else {
                Uri.fromFile(File(path))
            }
        } catch (exception: Exception) {
            Log.e(TAG, "getUriFromPath failed, ${exception.message}", exception)
            null
        }
    }

    private fun getFileExtensionFromUrl(url: String): String {
        var targetUrl = url
        if (TextUtils.isEmpty(targetUrl)) {
            return ""
        }
        val fragment = targetUrl.lastIndexOf('#')
        if (fragment > 0) {
            targetUrl = targetUrl.substring(0, fragment)
        }
        val query = targetUrl.lastIndexOf('?')
        if (query > 0) {
            targetUrl = targetUrl.substring(0, query)
        }
        val fileNamePosition = targetUrl.lastIndexOf('/')
        val fullFileName = if (fileNamePosition >= 0) {
            targetUrl.substring(fileNamePosition + 1)
        } else {
            targetUrl
        }
        if (fullFileName.isEmpty()) {
            return ""
        }
        val dotPosition = fullFileName.lastIndexOf('.')
        if (dotPosition < 0) {
            return ""
        }
        return fullFileName.substring(dotPosition + 1).lowercase(Locale.getDefault())
    }
}
