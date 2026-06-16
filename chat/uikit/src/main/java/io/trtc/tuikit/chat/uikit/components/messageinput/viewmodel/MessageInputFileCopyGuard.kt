package io.trtc.tuikit.chat.uikit.components.messageinput.viewmodel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

internal enum class MessageInputFileCopyResult {
    SUCCESS,
    TOO_LARGE,
    FAILED
}

internal object MessageInputFileCopyGuard {
    fun copyWithLimit(
        inputStream: InputStream,
        targetFile: File,
        maxBytes: Long
    ): MessageInputFileCopyResult {
        return try {
            targetFile.parentFile?.mkdirs()
            inputStream.use { input ->
                FileOutputStream(targetFile, false).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE_BYTES)
                    var totalBytes = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        totalBytes += read.toLong()
                        if (totalBytes > maxBytes) {
                            output.flush()
                            targetFile.delete()
                            return MessageInputFileCopyResult.TOO_LARGE
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            MessageInputFileCopyResult.SUCCESS
        } catch (_: IOException) {
            targetFile.delete()
            MessageInputFileCopyResult.FAILED
        } catch (_: SecurityException) {
            targetFile.delete()
            MessageInputFileCopyResult.FAILED
        }
    }

    fun sanitizeFileName(fileName: String): String? {
        val leafName = fileName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()
        if (leafName.isEmpty() || leafName.all { it == '.' }) {
            return null
        }
        val sanitized = leafName.mapNotNull { char ->
            if (char.code < CONTROL_CHAR_BOUNDARY || char == DELETE_CHAR) null else char
        }.joinToString("")
        return sanitized.ifEmpty { null }
    }

    private const val BUFFER_SIZE_BYTES = 8 * 1024
    private const val CONTROL_CHAR_BOUNDARY = 32
    private const val DELETE_CHAR = 127.toChar()
}
