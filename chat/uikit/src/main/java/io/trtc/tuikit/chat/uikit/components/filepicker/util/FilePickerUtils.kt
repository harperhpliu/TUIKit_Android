package io.trtc.tuikit.chat.uikit.components.filepicker.util
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.min

object FilePickerUtils {

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }

        return result
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var fileSize = -1L

        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        }

        if (fileSize == -1L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    fileSize = countBytes(inputStream)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return fileSize
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = context.contentResolver.getType(uri)

        if (mimeType == null) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            }
        }

        return mimeType
    }

    fun copyFileToAppDir(context: Context, uri: Uri, directory: String = "files"): File? {
        val fileName = sanitizeFileName(getFileName(context, uri) ?: return null) ?: return null

        val targetDir = File(context.filesDir, directory)
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return null
        }

        val targetFile = createUniqueFile(targetDir, fileName) ?: return null

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.use {
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (it.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            return targetFile
        } catch (e: IOException) {
            e.printStackTrace()
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }

        return null
    }

    fun countBytes(inputStream: InputStream): Long {
        val buffer = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val read = inputStream.read(buffer)
            if (read == -1) {
                break
            }
            total += read
        }
        return total
    }

    fun sanitizeFileName(fileName: String): String? {
        val leafName = fileName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()
        if (leafName.isEmpty() || leafName.all { it == '.' }) {
            return null
        }
        val sanitized = leafName.map { char ->
            if (char.code < 32 || char == 127.toChar()) '_' else char
        }.joinToString("")
        return sanitized.ifEmpty { null }
    }

    private fun createUniqueFile(directory: File, fileName: String): File? {
        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it != fileName }
            ?.let { ".$it" }
            .orEmpty()
        var candidate = File(directory, fileName)
        var index = 1
        while (candidate.exists()) {
            candidate = File(directory, "$baseName($index)$extension")
            index++
        }
        return candidate
    }

    fun readTextFromUri(context: Context, uri: Uri): String? {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                return inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = min((Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt(), units.lastIndex)
        return String.format(Locale.US, "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
