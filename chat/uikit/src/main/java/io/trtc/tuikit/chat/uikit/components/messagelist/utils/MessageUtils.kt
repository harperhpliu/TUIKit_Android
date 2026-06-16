package io.trtc.tuikit.chat.uikit.components.messagelist.utils
fun formatSmartTime(totalSeconds: Int?): String {
    if (totalSeconds == null || totalSeconds <= 0) return "00:00"
    return if (totalSeconds < 3600) {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    } else {
        val hours = totalSeconds / 3600
        val remaining = totalSeconds % 3600
        val minutes = remaining / 60
        val seconds = remaining % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}