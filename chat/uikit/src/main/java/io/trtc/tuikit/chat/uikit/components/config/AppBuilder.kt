package io.trtc.tuikit.chat.uikit.components.config
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tencent.mmkv.MMKV
import java.util.Locale

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class MessageAlignment {
    LEFT, RIGHT, TWO_SIDED
}

enum class MessageAction {
    COPY, RECALL, QUOTE, DELETE
}

enum class ConversationAction {
    DELETE, MUTE, PIN, MARK_UNREAD, CLEAR_HISTORY
}

enum class GlobalAvatarShape {
    CIRCULAR, SQUARE, ROUNDED
}

object AppBuilderConfig {
    private const val MMKV_ID = "atomicx_app_builder"
    private const val KEY_TRANSLATE_TARGET_LANGUAGE = "com.atomicx.translateTargetLanguage"

    var themeMode: ThemeMode = ThemeMode.SYSTEM
    var primaryColor: String = "#1C66E5"

    var messageAlignment: MessageAlignment = MessageAlignment.TWO_SIDED
    var enableReadReceipt: Boolean = false
    var messageActionList: List<MessageAction> = listOf(
        MessageAction.COPY,
        MessageAction.RECALL,
        MessageAction.QUOTE,
        MessageAction.DELETE
    )

    var enableCreateConversation: Boolean = true
    var conversationActionList: List<ConversationAction> = listOf(
        ConversationAction.DELETE,
        ConversationAction.MUTE,
        ConversationAction.PIN,
        ConversationAction.MARK_UNREAD,
        ConversationAction.CLEAR_HISTORY
    )

    var hideSendButton: Boolean = false

    var hideSearch: Boolean = false

    var avatarShape: GlobalAvatarShape = GlobalAvatarShape.ROUNDED

    private var mmkv: MMKV? = null
    private var translateTargetLanguageValue: String = Locale.getDefault().language

    internal fun initStorage(context: Context) {
        if (mmkv == null) {
            MMKV.initialize(context.applicationContext)
            mmkv = MMKV.mmkvWithID(MMKV_ID)
            val saved = mmkv?.decodeString(KEY_TRANSLATE_TARGET_LANGUAGE, "")
            if (!saved.isNullOrEmpty()) {
                translateTargetLanguageValue = saved
            }
        }
    }

    var translateTargetLanguage: String
        get() {
            val saved = mmkv?.decodeString(KEY_TRANSLATE_TARGET_LANGUAGE, "")
            if (!saved.isNullOrEmpty()) return saved
            return translateTargetLanguageValue
        }
        set(value) {
            translateTargetLanguageValue = value
            mmkv?.encode(KEY_TRANSLATE_TARGET_LANGUAGE, value)
        }
}

class AppBuilder private constructor() {
    private val configFileName = "appConfig.json"
    private var hasInit = false

    private fun loadConfigFromAssets(context: Context) {
        try {
            val jsonString = context.assets.open(configFileName)
                .bufferedReader()
                .use { it.readText() }

            val gson = Gson()
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
            parseConfig(jsonObject)
        } catch (e: Exception) {
            Log.w("AppBuilder", "Failed to load config from assets: ${e.message}")
        }
    }

    private fun loadConfig(context: Context) {
        if (!hasInit) {
            AppBuilderConfig.initStorage(context)
            loadConfigFromAssets(context)
            hasInit = true
        }
    }

    private fun parseConfig(json: JsonObject) {
        val config = AppBuilderConfig

        json.getAsJsonObject("theme")?.let { theme ->
            theme.get("mode")?.asString?.let { modeString ->
                when (modeString) {
                    "system" -> config.themeMode = ThemeMode.SYSTEM
                    "light" -> config.themeMode = ThemeMode.LIGHT
                    "dark" -> config.themeMode = ThemeMode.DARK
                }
            }

            theme.get("primaryColor")?.asString?.let { primaryColor ->
                config.primaryColor = primaryColor
            }
        }

        json.getAsJsonObject("messageList")?.let { messageList ->
            messageList.get("alignment")?.asString?.let { alignmentString ->
                when (alignmentString) {
                    "left" -> config.messageAlignment = MessageAlignment.LEFT
                    "right" -> config.messageAlignment = MessageAlignment.RIGHT
                    "two-sided" -> config.messageAlignment = MessageAlignment.TWO_SIDED
                }
            }

            messageList.get("enableReadReceipt")?.asBoolean?.let { enable ->
                config.enableReadReceipt = enable
            }

            messageList.getAsJsonArray("messageActionList")?.let { actionArray ->
                val actions = mutableListOf<MessageAction>()
                for (element in actionArray) {
                    when (element.asString) {
                        "copy" -> actions.add(MessageAction.COPY)
                        "recall" -> actions.add(MessageAction.RECALL)
                        "quote" -> actions.add(MessageAction.QUOTE)
                        "delete" -> actions.add(MessageAction.DELETE)
                    }
                }
                config.messageActionList = actions
            }
        }

        json.getAsJsonObject("conversationList")?.let { conversationList ->
            conversationList.get("enableCreateConversation")?.asBoolean?.let { enable ->
                config.enableCreateConversation = enable
            }

            conversationList.getAsJsonArray("conversationActionList")?.let { actionArray ->
                val actions = mutableListOf<ConversationAction>()
                for (element in actionArray) {
                    when (element.asString) {
                        "delete" -> actions.add(ConversationAction.DELETE)
                        "mute" -> actions.add(ConversationAction.MUTE)
                        "pin" -> actions.add(ConversationAction.PIN)
                        "markUnread" -> actions.add(ConversationAction.MARK_UNREAD)
                        "clearHistory" -> actions.add(ConversationAction.CLEAR_HISTORY)
                    }
                }
                config.conversationActionList = actions
            }
        }

        json.getAsJsonObject("messageInput")?.let { messageInput ->
            messageInput.get("hideSendButton")?.asBoolean?.let { hide ->
                config.hideSendButton = hide
            }
        }

        json.getAsJsonObject("search")?.let { search ->
            search.get("hideSearch")?.asBoolean?.let { hide ->
                config.hideSearch = hide
            }
        }

        json.getAsJsonObject("avatar")?.let { avatar ->
            avatar.get("shape")?.asString?.let { shapeString ->
                when (shapeString) {
                    "circular" -> config.avatarShape = GlobalAvatarShape.CIRCULAR
                    "square" -> config.avatarShape = GlobalAvatarShape.SQUARE
                    "rounded" -> config.avatarShape = GlobalAvatarShape.ROUNDED
                }
            }
        }
    }

    companion object {
        private val instance: AppBuilder by lazy { AppBuilder() }

        fun loadConfig(context: Context) {
            instance.loadConfig(context.applicationContext)
        }
    }
}
