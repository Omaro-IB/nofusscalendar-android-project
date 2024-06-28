
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DTUtils {
    companion object {
        // get number of days in a given month/year
        fun getMonthDays(year: Int, month: Int): Int {
            val x = LocalDate(year, month, 1)
            return when(month){
                12 -> x.daysUntil(LocalDate(year+1, 1, 1))
                else -> x.daysUntil(LocalDate(year, month+1, 1))
            }
        }

        // get the day of week (starting on sunday) of a given day/month/year
        fun dateToWeekDay(year: Int, month: Int, day: Int): Int {
            return when(val wd = LocalDate(year, month, day).dayOfWeek.isoDayNumber){
                7 -> 1
                else -> wd + 1
            }
        }

        // get all dates (in a 2D array of [[Y,M,D],...]) between two given dates (inclusive)
        fun getDaysBetween(year1: Int, month1: Int, day1: Int, year2: Int, month2: Int, day2: Int): Array<Array<Int>> {
            val date1 = LocalDate(year1, month1, day1)
            val date2 = LocalDate(year2, month2, day2)
            var date = date1
            var y: Int; var m: Int; var d: Int
            var returnArray: Array<Array<Int>> = arrayOf()
            while (date in (date1..date2)) {
                y = date.year; m = date.monthNumber; d = date.dayOfMonth
                returnArray += arrayOf(y, m, d)
                date = date.plus(1, DateTimeUnit.DAY)
            }
            return returnArray
        }

        // get current LocalDateTime
        fun getNow(): LocalDateTime{
            val currentMoment: Instant = Clock.System.now()
            val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            return datetimeInSystemZone
        }

        // get current month
        fun getMonth(): Int{
            return getNow().monthNumber
        }

        // get current year
        fun getYear(): Int{
            return getNow().year
        }

        // get current day
        fun getDay(): Int{
            return getNow().dayOfMonth
        }

        // convert month integer (1-12) to a string representation
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

        // convert weekday integer (1-7, starting sunday) to a string representation
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

        // convert given hour/minute integer time to a string representation
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
