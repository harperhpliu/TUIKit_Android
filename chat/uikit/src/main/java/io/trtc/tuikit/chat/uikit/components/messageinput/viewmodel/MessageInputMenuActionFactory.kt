package io.trtc.tuikit.chat.uikit.components.messageinput.viewmodel
import android.content.Context
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.chat.uikit.components.messageinput.config.MessageInputConfigProtocol
import io.trtc.tuikit.chat.uikit.components.messageinput.config.MessageInputMenuActionConfigProtocol
import io.trtc.tuikit.chat.uikit.components.messageinput.data.MessageInputMenuAction
import io.trtc.tuikit.chat.uikit.components.messageinput.data.MessageInputMenuActionContext
import io.trtc.tuikit.chat.uikit.components.messageinput.data.mergeMessageInputMenuActions

internal data class MessageInputMenuActionCallbacks(
    val onPickMedia: () -> Unit = {},
    val onCaptureImage: () -> Unit = {},
    val onRecordVideo: () -> Unit = {},
    val onPickFile: () -> Unit = {},
    val onStartAudioCall: () -> Unit = {},
    val onStartVideoCall: () -> Unit = {}
)

internal data class MessageInputMenuActionLabels(
    val album: String,
    val takePhoto: String,
    val recordVideo: String,
    val file: String,
    val audioCall: String,
    val videoCall: String
) {
    companion object {
        fun from(context: Context): MessageInputMenuActionLabels {
            return MessageInputMenuActionLabels(
                album = context.getString(R.string.message_input_album),
                takePhoto = context.getString(R.string.message_input_take_photo),
                recordVideo = context.getString(R.string.message_input_record_video),
                file = context.getString(R.string.message_input_file),
                audioCall = context.getString(R.string.message_input_audio_call),
                videoCall = context.getString(R.string.message_input_video_call)
            )
        }
    }
}

internal class MessageInputMenuActionFactory(
    private val config: MessageInputConfigProtocol,
    private val callbacks: MessageInputMenuActionCallbacks
) {
    fun create(context: Context, conversationID: String): List<MessageInputMenuAction> {
        val labels = MessageInputMenuActionLabels.from(context)
        return create(labels, context, conversationID)
    }

    fun create(
        labels: MessageInputMenuActionLabels,
        context: Context?,
        conversationID: String
    ): List<MessageInputMenuAction> {
        val actions = mutableListOf<MessageInputMenuAction>()
        actions += MessageInputMenuAction().apply {
            title = labels.album
            iconResID = R.drawable.message_input_menu_album_icon
            order = ACTION_ORDER_ALBUM
            onClick = callbacks.onPickMedia
        }
        if (config.isShowPhotoTaker) {
            actions += MessageInputMenuAction().apply {
                title = labels.takePhoto
                iconResID = R.drawable.message_input_menu_camera_icon
                order = ACTION_ORDER_TAKE_PHOTO
                onClick = callbacks.onCaptureImage
            }
            actions += MessageInputMenuAction().apply {
                title = labels.recordVideo
                iconResID = R.drawable.message_input_menu_record_icon
                order = ACTION_ORDER_RECORD_VIDEO
                onClick = callbacks.onRecordVideo
            }
        }
        actions += MessageInputMenuAction().apply {
            title = labels.file
            iconResID = R.drawable.message_input_menu_file_icon
            order = ACTION_ORDER_FILE
            onClick = callbacks.onPickFile
        }
        if (config.isShowVideoCall) {
            actions += MessageInputMenuAction().apply {
                title = labels.videoCall
                iconResID = R.drawable.message_input_menu_video_call_icon
                order = ACTION_ORDER_VIDEO_CALL
                onClick = callbacks.onStartVideoCall
            }
        }
        if (config.isShowAudioCall) {
            actions += MessageInputMenuAction().apply {
                title = labels.audioCall
                iconResID = R.drawable.message_input_menu_audio_call_icon
                order = ACTION_ORDER_AUDIO_CALL
                onClick = callbacks.onStartAudioCall
            }
        }
        val customActions = if (context == null) {
            emptyList()
        } else {
            (config as? MessageInputMenuActionConfigProtocol)
                ?.customMenuActionProvider
                ?.getActions(
                    MessageInputMenuActionContext(
                        context = context,
                        conversationID = conversationID
                    )
                )
                .orEmpty()
        }
        return mergeMessageInputMenuActions(actions, customActions)
    }

    private companion object {
        const val ACTION_ORDER_ALBUM = 100
        const val ACTION_ORDER_TAKE_PHOTO = 200
        const val ACTION_ORDER_RECORD_VIDEO = 300
        const val ACTION_ORDER_FILE = 400
        const val ACTION_ORDER_VIDEO_CALL = 500
        const val ACTION_ORDER_AUDIO_CALL = 600
    }
}
