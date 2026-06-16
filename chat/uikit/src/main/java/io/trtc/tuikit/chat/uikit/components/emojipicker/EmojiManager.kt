package io.trtc.tuikit.chat.uikit.components.emojipicker
import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.chat.uikit.components.emojipicker.model.Emoji
import io.trtc.tuikit.chat.uikit.components.emojipicker.model.EmojiGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EmojiManager {

    private var _isInitialized = false
    private val _littleEmojiList = mutableListOf<Emoji>()
    private val _littleEmojiKeyList = mutableListOf<String>()
    private val _emojiGroupList = mutableListOf<EmojiGroup>()
    private val _customEmojiGroupList = mutableListOf<EmojiGroup>()
    private val _emojiByKeyMap = mutableMapOf<String, Emoji>()

    private var _sortedLittleEmojiList: List<Emoji> = emptyList()
    private var _sortedLittleEmojiKeyList: List<String> = emptyList()

    private val _emojiImageCache = mutableMapOf<String, Drawable>()
    private var _isPreloadingImages = false

    private val _emojiGroupState = MutableStateFlow<List<EmojiGroup>>(emptyList())
    val emojiGroupState: StateFlow<List<EmojiGroup>> = _emojiGroupState.asStateFlow()

    private var _emojiIndexVersion = 0
    val emojiIndexVersion: Int
        get() = synchronized(this) { _emojiIndexVersion }

    val littleEmojiList: List<Emoji>
        get() = synchronized(this) { _littleEmojiList.toList() }

    val littleEmojiKeyList: List<String>
        get() = synchronized(this) { _littleEmojiKeyList.toList() }

    val sortedLittleEmojiList: List<Emoji>
        get() = synchronized(this) { _sortedLittleEmojiList.toList() }

    val sortedLittleEmojiKeyList: List<String>
        get() = synchronized(this) { _sortedLittleEmojiKeyList.toList() }

    val emojiGroupList: List<EmojiGroup>
        get() = _emojiGroupState.value

    fun initialize(context: Context) {
        synchronized(this) {
            if (_isInitialized) return
        }

        val builtInEmojiGroup = try {
            val emojiKeys: Array<String> = context.resources
                .getStringArray(R.array.emoji_picker_key_array)
            val emojiNames: Array<String> = context.resources
                .getStringArray(R.array.emoji_picker_name_array)
            val emojiPath: Array<String> = context.resources
                .getStringArray(R.array.emoji_picker_file_name_array)

            require(emojiKeys.size == emojiNames.size && emojiKeys.size == emojiPath.size) {
                "Emoji resource arrays must have the same size"
            }

            val emojis = mutableListOf<Emoji>()

            for (i in emojiKeys.indices) {
                val emojiKey = emojiKeys[i]
                val emojiName = emojiNames[i]
                val emojiUrl = "file:///android_asset/buildinemojis/" + emojiPath[i]
                val emoji = Emoji(emojiKey, emojiName, emojiUrl)
                emojis.add(emoji)
            }

            EmojiGroup(
                id = "LittleEmoji",
                "LittleYellowFaceEmoji",
                emojiGroupIconUrl = emojis.firstOrNull()?.emojiUrl ?: "",
                emojis = emojis,
                isLittleEmoji = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        synchronized(this) {
            if (_isInitialized) return
            _emojiGroupList.clear()
            _emojiGroupList.add(builtInEmojiGroup)
            rebuildEmojiIndexLocked()
            _isInitialized = true
        }

        preloadEmojiImages(context)
    }

    fun registerEmojiGroup(emojiGroup: EmojiGroup) {
        synchronized(this) {
            _customEmojiGroupList.removeAll { it.id == emojiGroup.id }
            _customEmojiGroupList.add(emojiGroup)
            rebuildEmojiIndexLocked()
        }
    }

    fun registerEmojiGroups(emojiGroups: List<EmojiGroup>) {
        synchronized(this) {
            val uniqueEmojiGroups = linkedMapOf<String, EmojiGroup>()
            emojiGroups.forEach { uniqueEmojiGroups[it.id] = it }
            val ids = uniqueEmojiGroups.keys
            _customEmojiGroupList.removeAll { it.id in ids }
            _customEmojiGroupList.addAll(uniqueEmojiGroups.values)
            rebuildEmojiIndexLocked()
        }
    }

    fun removeCustomEmojiGroup(id: String) {
        synchronized(this) {
            _customEmojiGroupList.removeAll { it.id == id }
            rebuildEmojiIndexLocked()
        }
    }

    fun clearCustomEmojiGroups() {
        synchronized(this) {
            _customEmojiGroupList.clear()
            rebuildEmojiIndexLocked()
        }
    }

    fun findEmojiByKey(key: String): Emoji? {
        return synchronized(this) { _emojiByKeyMap[key] }
    }

    fun containsEmojiKey(text: String): Boolean {
        return synchronized(this) { _littleEmojiKeyList.any { text.contains(it) } }
    }

    fun getCachedEmojiDrawable(key: String): Drawable? {
        val cachedDrawable = synchronized(this) { _emojiImageCache[key] } ?: return null
        return cachedDrawable.constantState?.newDrawable()?.mutate()
    }

    private fun rebuildEmojiIndexLocked() {
        _littleEmojiList.clear()
        _littleEmojiKeyList.clear()
        _emojiByKeyMap.clear()

        val allGroups = _emojiGroupList + _customEmojiGroupList
        allGroups.forEach { group ->
            group.emojis.forEach { emoji ->
                _emojiByKeyMap[emoji.key] = emoji
                if (group.isLittleEmoji) {
                    _littleEmojiList.add(emoji)
                    _littleEmojiKeyList.add(emoji.key)
                }
            }
        }

        _sortedLittleEmojiList = _littleEmojiList.sortedByDescending { it.key.length }
        _sortedLittleEmojiKeyList = _littleEmojiKeyList.sortedByDescending { it.length }
        _emojiGroupState.value = allGroups.toList()
        _emojiIndexVersion += 1
    }

    private fun preloadEmojiImages(context: Context) {
        val emojisToPreload = synchronized(this) {
            if (_isPreloadingImages) return
            _isPreloadingImages = true
            _littleEmojiList.toList()
        }

        try {
            emojisToPreload.forEach { emoji ->
                if (synchronized(this) { !_emojiImageCache.containsKey(emoji.key) }) {
                    Glide.with(context.applicationContext)
                        .asDrawable()
                        .load(emoji.emojiUrl)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                if (resource.constantState != null) {
                                    synchronized(this@EmojiManager) {
                                        _emojiImageCache[emoji.key] = resource
                                    }
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            synchronized(this) {
                _isPreloadingImages = false
            }
        }
    }

    fun clearImageCache() {
        synchronized(this) {
            _emojiImageCache.clear()
        }
    }

    fun getCacheSize(): Int {
        return synchronized(this) { _emojiImageCache.size }
    }
}
