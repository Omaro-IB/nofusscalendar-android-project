import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

class DTUtils {
    companion object {
        fun getMonthDays(year: Int, month: Int): Int {
            val x = LocalDate(year, month, 1)
            return when(month){
                12 -> x.daysUntil(LocalDate(year+1, 1, 1))
                else -> x.daysUntil(LocalDate(year, month+1, 1))
            }
        }

        fun dateToWeekDay(year: Int, month: Int, day: Int): Int {
            return when(val wd = LocalDate(year, month, day).dayOfWeek.isoDayNumber){
                7 -> 1
                else -> wd + 1
            }
        }

        fun getNow(): LocalDateTime{
            val currentMoment: Instant = Clock.System.now()
            val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            return datetimeInSystemZone
        }

        fun getMonth(): Int{
            return getNow().monthNumber
        }

        fun getYear(): Int{
            return getNow().year
        }

        fun getDay(): Int{
            return getNow().dayOfMonth
        }

        fun monthIntToStr(month: Int, short: Boolean = false): String{
            if (short) {
                return when(month){
                    1 -> "Jan"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Apr"
                    5 -> "May"
                    6 -> "Jun"
                    7 -> "Jul"
                    8 -> "Aug"
                    9 -> "Sep"
                    10 -> "Oct"
                    11 -> "Nov"
                    else -> "Dec"
                }
            } else {
                return when(month){
                    1 -> "January"
                    2 -> "February"
                    3 -> "March"
                    4 -> "April"
                    5 -> "May"
                    6 -> "June"
                    7 -> "July"
                    8 -> "August"
                    9 -> "September"
                    10 -> "October"
                    11 -> "November"
                    else -> "December"
                }
            }
        }

        fun weekDayIntToStr(weekday: Int, short: Boolean = false): String{
            if (short) {
                return when(weekday){
                    1 -> "Sun"
                    2 -> "Mon"
                    3 -> "Tue"
                    4 -> "Wed"
                    5 -> "Thu"
                    6 -> "Fr"
                    else -> "Sat"
                }
            } else {
                return when (weekday) {
                    1 -> "Sunday"
                    2 -> "Monday"
                    3 -> "Tuesday"
                    4 -> "Wednesday"
                    5 -> "Thursday"
                    6 -> "Friday"
                    else -> "Saturday"
                }
            }
        }

        fun timeToStr(hour: Int, minute: Int, military: Boolean = false): String {
            if (military) {
                return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            } else {
                var ending = "AM"
                var nHour = hour
                if (hour >= 12) {
                    ending = "PM"
                }
                if (hour > 12) {
                    nHour -= 12
                }
                return "$nHour:${minute.toString().padStart(2, '0')} $ending"
            }
        }
    }
}
