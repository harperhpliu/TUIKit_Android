package io.trtc.tuikit.chat.uikit.components.conversationlist.utils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ChatTimeUtil {

    private data class DateFormatEntry(
        val locale: Locale,
        val formatter: SimpleDateFormat
    )

    private val dateFormatHolder = ThreadLocal<DateFormatEntry>()

    fun getTimeFormatText(timeStamp: Long?): String {
        if (timeStamp == null || timeStamp <= 0) return ""

        val millis = timeStamp * 1000L
        val date = Date(millis)
        if (date == Date(Long.MIN_VALUE)) return ""

        val locale = Locale.getDefault()
        val dateFmt = dateFormatHolder.get()
            ?.takeIf { it.locale == locale }
            ?.formatter
            ?: SimpleDateFormat().also {
                dateFormatHolder.set(DateFormatEntry(locale, it))
            }
        dateFmt.apply {
            timeZone = TimeZone.getDefault()
        }

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
        }

        val now = Date()
        calendar.time = now
        val nowDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val nowMonth = calendar.get(Calendar.MONTH)
        val nowYear = calendar.get(Calendar.YEAR)
        val nowWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

        calendar.time = date
        val dateDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dateMonth = calendar.get(Calendar.MONTH)
        val dateYear = calendar.get(Calendar.YEAR)
        val dateWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

        return when {
            nowYear != dateYear -> {
                dateFmt.apply { applyPattern("yyyy/MM/dd") }.format(date)
            }
            nowMonth != dateMonth -> {
                dateFmt.apply { applyPattern("MM/dd") }.format(date)
            }
            nowWeekOfMonth != dateWeekOfMonth -> {
                dateFmt.apply { applyPattern("MM/dd") }.format(date)
            }
            nowDayOfMonth != dateDayOfMonth -> {
                dateFmt.apply { applyPattern("EEEE") }.format(date)
            }
            else -> {
                dateFmt.apply { applyPattern("HH:mm") }.format(date)
            }
        }
    }
}
