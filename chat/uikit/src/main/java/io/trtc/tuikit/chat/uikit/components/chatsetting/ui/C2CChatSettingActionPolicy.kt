package io.trtc.tuikit.chat.uikit.components.chatsetting.ui

internal enum class C2CChatSettingAction {
    SEND_MESSAGE,
    VOICE_CALL,
    VIDEO_CALL,
    CLEAR_HISTORY,
    DELETE_FRIEND
}

internal object C2CChatSettingActionPolicy {

    fun actions(): List<C2CChatSettingAction> {
        return listOf(
            C2CChatSettingAction.SEND_MESSAGE,
            C2CChatSettingAction.VOICE_CALL,
            C2CChatSettingAction.VIDEO_CALL,
            C2CChatSettingAction.CLEAR_HISTORY,
            C2CChatSettingAction.DELETE_FRIEND
        )
    }
}
