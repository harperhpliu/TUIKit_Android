package io.trtc.tuikit.chat.uikit.components.messagelist.adapter
import io.trtc.tuikit.chat.uikit.components.messagelist.config.MessageBubbleAppearance
import io.trtc.tuikit.chat.uikit.components.messagelist.config.MessageListConfigProtocol
import io.trtc.tuikit.chat.uikit.components.messagelist.ui.BubbleStyle

object MessageBubbleBinder {
    fun resolveBubbleStyle(
        bubbleStyle: BubbleStyle,
        hasReactions: Boolean,
        config: MessageListConfigProtocol? = null,
        isLeftAligned: Boolean = true,
        isSelf: Boolean = false
    ): MessageBubbleStyleBinding {
        val wrapMediaInDefaultBubble = bubbleStyle == BubbleStyle.NONE && hasReactions
        val effectiveStyle = if (wrapMediaInDefaultBubble) BubbleStyle.DEFAULT else bubbleStyle
        return MessageBubbleStyleBinding(
            effectiveStyle = effectiveStyle,
            wrapMediaInDefaultBubble = wrapMediaInDefaultBubble,
            appearance = resolveAppearance(
                effectiveStyle = effectiveStyle,
                config = config,
                isLeftAligned = isLeftAligned,
                isSelf = isSelf
            )
        )
    }

    private fun resolveAppearance(
        effectiveStyle: BubbleStyle,
        config: MessageListConfigProtocol?,
        isLeftAligned: Boolean,
        isSelf: Boolean
    ): MessageBubbleAppearance? {
        if (effectiveStyle != BubbleStyle.DEFAULT || config == null) {
            return null
        }
        val positionAppearance = if (isLeftAligned) {
            config.leftBubbleAppearance
        } else {
            config.rightBubbleAppearance
        }
        val senderAppearance = if (isSelf) {
            config.ownBubbleAppearance
        } else {
            config.incomingBubbleAppearance
        }
        val base = config.defaultBubbleAppearance ?: MessageBubbleAppearance()
        val resolved = base
            .mergeWith(positionAppearance)
            .mergeWith(senderAppearance)
        return resolved.takeUnless { it.isEmpty }
    }
}

data class MessageBubbleStyleBinding(
    val effectiveStyle: BubbleStyle,
    val wrapMediaInDefaultBubble: Boolean,
    val appearance: MessageBubbleAppearance? = null
)
