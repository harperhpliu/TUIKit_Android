package io.trtc.tuikit.chat.uikit.components.messageinput.config
import io.trtc.tuikit.chat.uikit.components.messageinput.data.MessageInputMenuActionProvider

interface MessageInputConfigProtocol {
    val isShowAudioRecorder: Boolean
    val isShowPhotoTaker: Boolean
    val isShowAudioCall: Boolean
    val isShowVideoCall: Boolean
    val isShowMore: Boolean
    val enableMention: Boolean
    val audioMaxRecordDurationMs: Int
        get() = 60 * 1000
}

interface MessageInputMenuActionConfigProtocol {
    val customMenuActionProvider: MessageInputMenuActionProvider?
}

class ChatMessageInputConfig : MessageInputConfigProtocol, MessageInputMenuActionConfigProtocol {

    private var _isShowAudioRecorder: Boolean? = null
    private var _isShowPhotoTaker: Boolean? = null
    private var _isShowAudioCall: Boolean? = null
    private var _isShowVideoCall: Boolean? = null
    private var _isShowMore: Boolean? = null
    private var _enableMention: Boolean? = null
    private var _audioMaxRecordDurationMs: Int? = null
    private var _customMenuActionProvider: MessageInputMenuActionProvider? = null

    constructor(
        isShowAudioRecorder: Boolean? = null,
        isShowPhotoTaker: Boolean? = null,
        isShowAudioCall: Boolean? = null,
        isShowVideoCall: Boolean? = null,
        isShowMore: Boolean? = null,
        enableMention: Boolean? = null,
        audioMaxRecordDurationMs: Int? = null
    ) {
        this._isShowAudioRecorder = isShowAudioRecorder
        this._isShowPhotoTaker = isShowPhotoTaker
        this._isShowAudioCall = isShowAudioCall
        this._isShowVideoCall = isShowVideoCall
        this._isShowMore = isShowMore
        this._enableMention = enableMention
        this._audioMaxRecordDurationMs = audioMaxRecordDurationMs
    }

    override var isShowAudioRecorder: Boolean
        get() = _isShowAudioRecorder ?: true
        set(value) {
            _isShowAudioRecorder = value
        }

    override var isShowPhotoTaker: Boolean
        get() = _isShowPhotoTaker ?: true
        set(value) {
            _isShowPhotoTaker = value
        }

    override var isShowAudioCall: Boolean
        get() = _isShowAudioCall ?: true
        set(value) {
            _isShowAudioCall = value
        }

    override var isShowVideoCall: Boolean
        get() = _isShowVideoCall ?: true
        set(value) {
            _isShowVideoCall = value
        }

    override var isShowMore: Boolean
        get() = _isShowMore ?: true
        set(value) {
            _isShowMore = value
        }

    override var enableMention: Boolean
        get() = _enableMention ?: true
        set(value) {
            _enableMention = value
        }

    override var audioMaxRecordDurationMs: Int
        get() = _audioMaxRecordDurationMs ?: 60 * 1000
        set(value) {
            _audioMaxRecordDurationMs = value
        }

    override val customMenuActionProvider: MessageInputMenuActionProvider?
        get() = _customMenuActionProvider

    fun setCustomMenuActionProvider(provider: MessageInputMenuActionProvider?): ChatMessageInputConfig {
        _customMenuActionProvider = provider
        return this
    }
}
