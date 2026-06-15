package io.trtc.tuikit.chat.uikit.components.messagelist.viewmodel
import com.tencent.cloud.tuikit.engine.common.ContextProvider
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.atomicxcore.api.message.MessageInfo
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

internal class MessageListTimeGroupingPolicy(
    private val aggregationSeconds: Int = MESSAGE_AGGREGATION_TIME,
    private val nowProvider: () -> Date = { Date() },
    private val localeProvider: () -> Locale = { Locale.getDefault() },
    private val localizedYesterdayProvider: () -> String = {
        ContextProvider.getApplicationContext()
            ?.getString(R.string.message_list_time_yesterday)
            ?: "Yesterday"
    }
) {

    fun timeStringForMessageAt(index: Int, messages: List<MessageInfo>): String? {
        val message = messages.getOrNull(index)
        if (index == messages.lastIndex) {
            return formatTime(message?.timestamp?.times(1000))
        }
        val previousMessage = messages.getOrNull(index + 1)
        if (message != null && previousMessage != null) {
            val timeInterval = getIntervalSeconds(message.timestamp, previousMessage.timestamp)
            if (timeInterval > aggregationSeconds) {
                return formatTime(message.timestamp?.times(1000))
            }
        }
        return null
    }

    private fun getIntervalSeconds(ts1: Long?, ts2: Long?): Long {
        if (ts1 == null || ts2 == null) return 0L
        if (ts1 == 0L || ts2 == 0L) return 0L
        return abs(ts1 - ts2)
    }

    private fun formatTime(timestampMs: Long?): String? {
        val date = timestampMs?.let { Date(it) } ?: return null
        if (date.time == 0L) return null

        val locale = localeProvider()
        val now = nowProvider()
        val timeString = formatDate(date, "HH:mm", Locale.US)

        val nowCalendar = calendarFor(now)
        val dateCalendar = calendarFor(date)
        return when {
            isSameDay(nowCalendar, dateCalendar) -> timeString
            isYesterday(nowCalendar, dateCalendar) -> "${localizedYesterdayProvider()} $timeString"
            isSameYear(nowCalendar, dateCalendar) && isSameWeek(nowCalendar, dateCalendar) -> {
                "${formatDate(date, "EEEE", locale)} $timeString"
            }
            isSameYear(nowCalendar, dateCalendar) -> "${formatDate(date, "M/d", Locale.US)} $timeString"
            else -> "${formatDate(date, "yyyy/M/d", Locale.US)} $timeString"
        }
    }

    private fun formatDate(date: Date, pattern: String, locale: Locale): String {
        return SimpleDateFormat(pattern, locale).apply {
            numberFormat = DecimalFormat("0", DecimalFormatSymbols(Locale.US)).apply {
                isGroupingUsed = false
            }
        }.format(date)
    }

    private fun calendarFor(date: Date): Calendar {
        return Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            time = date
        }
    }

    private fun isSameDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(nowCalendar: Calendar, dateCalendar: Calendar): Boolean {
        val yesterday = startOfDay(nowCalendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, dateCalendar)
    }

    private fun isSameYear(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
    }

    private fun isSameWeek(first: Calendar, second: Calendar): Boolean {
        return isSameDay(startOfWeek(first), startOfWeek(second))
    }

    private fun startOfWeek(calendar: Calendar): Calendar {
        return startOfDay(calendar).apply {
            val mondayBasedIndex = (get(Calendar.DAY_OF_WEEK) + 5) % 7
            add(Calendar.DAY_OF_YEAR, -mondayBasedIndex)
        }
    }

    private fun startOfDay(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
