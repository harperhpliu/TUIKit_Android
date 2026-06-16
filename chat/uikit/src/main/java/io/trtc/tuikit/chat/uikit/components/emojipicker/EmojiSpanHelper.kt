package io.trtc.tuikit.chat.uikit.components.emojipicker
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.LruCache
import android.view.View
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object EmojiSpanHelper {

    private const val EMOJI_KEY_PREFIX = "[TUIEmoji_"
    private const val TEXT_REPLACEMENT_CACHE_SIZE = 200
    private val textReplacementCache = object : LruCache<String, String>(TEXT_REPLACEMENT_CACHE_SIZE) {}

    fun setEmojiSpanText(
        context: Context,
        text: String,
        textSizePx: Float,
        requestView: View? = null,
        onResult: (CharSequence) -> Unit
    ) {
        if (text.isEmpty()) {
            onResult(text)
            return
        }

        EmojiManager.initialize(context)

        val emojiSize = (textSizePx * 1.5f).toInt()
        val spannable = SpannableStringBuilder(text)
        val sortedKeys = EmojiManager.sortedLittleEmojiKeyList

        val pendingLoads = mutableListOf<Triple<Int, Int, String>>()

        for (emojiKey in sortedKeys) {
            var startIndex = spannable.indexOf(emojiKey)
            while (startIndex != -1) {
                val endIndex = startIndex + emojiKey.length
                val cachedDrawable = EmojiManager.getCachedEmojiDrawable(emojiKey)
                if (cachedDrawable != null) {
                    cachedDrawable.setBounds(0, 0, emojiSize, emojiSize)
                    val imageSpan = CenterImageSpan(cachedDrawable)
                    spannable.setSpan(
                        imageSpan,
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    pendingLoads.add(Triple(startIndex, endIndex, emojiKey))
                }
                startIndex = spannable.indexOf(emojiKey, endIndex)
            }
        }

        if (pendingLoads.isEmpty()) {
            onResult(spannable)
            return
        }

        val remaining = AtomicInteger(pendingLoads.size)
        for ((start, end, key) in pendingLoads) {
            val emoji = EmojiManager.findEmojiByKey(key)
            if (emoji == null) {
                if (remaining.decrementAndGet() == 0) onResult(spannable)
                continue
            }
            glideWith(context, requestView)
                .asDrawable()
                .load(emoji.emojiUrl)
                .into(object : CustomTarget<Drawable>() {
                    private val completed = AtomicBoolean(false)

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        val spanDrawable = resource.newDrawableForSpan()
                        spanDrawable.setBounds(0, 0, emojiSize, emojiSize)
                        if (start < spannable.length && end <= spannable.length) {
                            val imageSpan = CenterImageSpan(spanDrawable)
                            spannable.setSpan(
                                imageSpan,
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        completePendingLoad(remaining, completed, spannable, onResult)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        completePendingLoad(remaining, completed, spannable, onResult)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        completePendingLoad(remaining, completed, spannable, onResult)
                    }
                })
        }
    }

    fun processEditTextEmoji(editText: EditText) {
        val content = editText.text ?: return
        if (content.isEmpty()) return

        val context = editText.context
        EmojiManager.initialize(context)

        val textSizePx = editText.textSize
        val emojiSize = (textSizePx * 1.5f).toInt()
        val spannable = content
        val emojiKeys = EmojiManager.littleEmojiKeyList
        val sortedKeys = EmojiManager.sortedLittleEmojiKeyList

        val existingSpans = spannable.getSpans(0, spannable.length, CenterImageSpan::class.java)
        val existingSpanRanges = mutableSetOf<Pair<Int, Int>>()
        val invalidSpans = mutableListOf<CenterImageSpan>()
        val pendingLoads = mutableListOf<Triple<Int, Int, String>>()
        val originalText = content.toString()

        for (span in existingSpans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            if (start >= 0 && end <= spannable.length) {
                val spanText = spannable.substring(start, end)
                if (emojiKeys.contains(spanText)) {
                    existingSpanRanges.add(start to end)
                } else {
                    invalidSpans.add(span)
                }
            } else {
                invalidSpans.add(span)
            }
        }

        for (span in invalidSpans) {
            spannable.removeSpan(span)
        }

        var hasChanges = false

        for (emojiKey in sortedKeys) {
            var startIndex = spannable.indexOf(emojiKey)
            while (startIndex != -1) {
                val endIndex = startIndex + emojiKey.length
                val range = startIndex to endIndex

                if (startIndex >= 0 && endIndex <= spannable.length && !existingSpanRanges.contains(range)) {
                    val cachedDrawable = EmojiManager.getCachedEmojiDrawable(emojiKey)
                    if (cachedDrawable != null) {
                        cachedDrawable.setBounds(0, 0, emojiSize, emojiSize)
                        val imageSpan = CenterImageSpan(cachedDrawable)
                        spannable.setSpan(
                            imageSpan,
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        hasChanges = true
                    } else {
                        pendingLoads.add(Triple(startIndex, endIndex, emojiKey))
                    }
                }

                startIndex = spannable.indexOf(emojiKey, endIndex)
            }
        }

        if (hasChanges) {
            val currentSelection = editText.selectionStart
            if (currentSelection >= 0 && currentSelection <= editText.text.length) {
                editText.setSelection(currentSelection)
            }
        }

        if (pendingLoads.isNotEmpty()) {
            loadMissingEditTextEmojiSpans(editText, originalText, pendingLoads, emojiSize)
        }
    }

    fun replaceEmojiKeysWithNames(text: String): String {
        if (text.isEmpty()) return text
        val cacheKey = "${EmojiManager.emojiIndexVersion}:$text"
        textReplacementCache.get(cacheKey)?.let { return it }

        if (!text.contains(EMOJI_KEY_PREFIX) && !EmojiManager.containsEmojiKey(text)) {
            textReplacementCache.put(cacheKey, text)
            return text
        }

        val sortedEmojis = EmojiManager.sortedLittleEmojiList
        if (sortedEmojis.isEmpty()) return text

        var result = text
        sortedEmojis.forEach { emoji ->
            if (result.contains(emoji.key)) {
                result = result.replace(emoji.key, emoji.emojiName)
            }
        }

        textReplacementCache.put(cacheKey, result)
        return result
    }

    fun clearTextReplacementCache() {
        textReplacementCache.evictAll()
    }

    private fun loadMissingEditTextEmojiSpans(
        editText: EditText,
        expectedText: String,
        pendingLoads: List<Triple<Int, Int, String>>,
        emojiSize: Int
    ) {
        for ((start, end, key) in pendingLoads) {
            val emoji = EmojiManager.findEmojiByKey(key) ?: continue
            Glide.with(editText)
                .asDrawable()
                .load(emoji.emojiUrl)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        val spanDrawable = resource.newDrawableForSpan()
                        spanDrawable.setBounds(0, 0, emojiSize, emojiSize)
                        editText.post {
                            if (!editText.isAttachedToWindow) return@post
                            val editable = editText.text ?: return@post
                            if (editable.toString() != expectedText) return@post
                            if (start < 0 || end > editable.length || editable.substring(start, end) != key) {
                                return@post
                            }
                            val existingSpans = editable.getSpans(start, end, CenterImageSpan::class.java)
                            val hasExistingSpan = existingSpans.any { span ->
                                editable.getSpanStart(span) == start && editable.getSpanEnd(span) == end
                            }
                            if (!hasExistingSpan) {
                                editable.setSpan(
                                    CenterImageSpan(spanDrawable),
                                    start,
                                    end,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                editText.invalidate()
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun glideWith(context: Context, requestView: View?): RequestManager {
        return if (requestView != null) {
            Glide.with(requestView)
        } else {
            Glide.with(context.applicationContext)
        }
    }

    private fun completePendingLoad(
        remaining: AtomicInteger,
        completed: AtomicBoolean,
        spannable: SpannableStringBuilder,
        onResult: (CharSequence) -> Unit
    ) {
        if (completed.compareAndSet(false, true) && remaining.decrementAndGet() == 0) {
            onResult(spannable)
        }
    }

    private fun Drawable.newDrawableForSpan(): Drawable {
        return constantState?.newDrawable()?.mutate() ?: mutate()
    }
}
