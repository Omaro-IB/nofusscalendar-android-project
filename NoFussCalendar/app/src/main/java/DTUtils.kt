/*
 * DTUtils
 * Use Date object as high level API to kotlinx.datetime
 * Useful for Calendar operations, keeping code consistent, readable, and concise
 */

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

enum class TimeUnit {YEAR, MONTH, WEEK, DAY}
enum class DateFormat {DAYMONTHYEARSHORT, DAYMONTHYEARLONG, MONTHYEARSHORT, MONTHYEARLONG, FULLSHORT, FULLLONG, YYYYMMDD, ISO}

data class Date(private val year: Int, private val month: Int, private val day: Int) {
    /**
     * Creates Date object corresponding to the given [year], [month], and [day]
     * Encapsulates [LocalDate] from kotlinx.datetime
     */
    private var dateYear = year
    private var dateMonth = month
    private var dateDay = day
    private var dateObject = LocalDate(year, month, day)

    private fun synchronizeIntegers() { dateYear = dateObject.year; dateMonth = dateObject.monthNumber; dateDay = dateObject.dayOfMonth }
    private fun synchronizeDate() { dateObject = LocalDate(dateYear, dateMonth, dateDay) }

    fun changeDate(timeUnit: TimeUnit, amount: Int) {
        /**
         * Change date by a given [timeUnit] [amount]
         * [timeUnit]: TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.WEEK, TimeUnit.DAY
         * [amount]: Int specifying number of [timeUnit]s
         */
        when (timeUnit) {
            TimeUnit.YEAR -> {val period = DatePeriod(years = amount); dateObject = dateObject.plus(period)}
            TimeUnit.MONTH -> {val period = DatePeriod(months = amount); dateObject = dateObject.plus(period)}
            TimeUnit.WEEK -> {val period = DatePeriod(days = 7*amount); dateObject = dateObject.plus(period)}
            TimeUnit.DAY -> {val period = DatePeriod(days = amount); dateObject = dateObject.plus(period)}
        }
        synchronizeIntegers()

    }

    /**
     * Get methods; use to manually find integer year/month/day values
     */
    fun getYear(): Int { return dateYear }
    fun getMonthOfYear(): Int { return dateMonth }
    fun getDayOfMonth(): Int { return dateDay }

    /**
     * Set methods; use to manually set integer year/month/day values
     */
    fun setYear(year: Int) { dateYear = year; synchronizeDate() }
    fun setMonthOfYear(month: Int) { dateMonth = month; synchronizeDate() }
    fun setDayOfMonth(day: Int) { dateDay = day; synchronizeDate() }

    fun getDayOfWeek(): Int {
        /**
         * Get the current date's day of week as integer
         * Sunday = 1, Saturday = 7, etc.
         */
        return when(val wd = dateObject.dayOfWeek.isoDayNumber){
            7 -> 1
            else -> wd + 1
        }
    }

    fun getDayOfWeekOfFirstOfMonth(): Int {
        /**
         * Get the current date's month's first day's day of week as integer
         * Sunday = 1, Saturday = 7, etc.
         */
        return when(val wd = LocalDate(dateYear, dateMonth, 1).dayOfWeek.isoDayNumber){
            7 -> 1
            else -> wd + 1
        }
    }

    fun getNumDaysOfMonth(): Int {
        /**
         * Get the number of days in the current date's month'
         */
        val x = LocalDate(dateYear, dateMonth, 1)
        return when(dateMonth){
            12 -> x.daysUntil(LocalDate(dateYear+1, 1, 1))
            else -> x.daysUntil(LocalDate(dateYear, dateMonth+1, 1))
        }
    }

    fun subtractDays(other: Date): Int {
        /**
         * Find difference this date - other date
         * (+ve if this date is later, -ve if this date is earlier)
         */
        return other.dateObject.daysUntil(dateObject)
    }

    fun isPastDate(other: Date): Boolean {
        /**
         * return true if this date is past other date, false otherwise
         */
        return subtractDays(other) > 0
    }

    fun isBeforeNextMonth(year: Int, month: Int): Boolean {
        /**
         * return true if this date is before start of next given year/month
         */
        val startOfMonth = LocalDate(year, month, 1).plus(DatePeriod(months = 1))
        return dateObject.daysUntil(startOfMonth) > 0
    }

    fun isInMonth(year: Int, month: Int): Boolean {
        /**
         * return true if this date is in given year/month
         */
        val startOfMonth = LocalDate(year, month, 1)
        val endOfMonth = LocalDate(year, month+1, 1).plus(DatePeriod(days = -1))
        return dateObject.daysUntil(startOfMonth) <= 0 && dateObject.daysUntil(endOfMonth) >= 0
    }

    fun formatAsString(format: DateFormat): String {
        /**
         * Format as string, specify format using [format]: [DateFormat]
         * [DateFormat.DAYMONTHYEARLONG] -> January 1 2001
         * [DateFormat.DAYMONTHYEARSHORT] -> Jan 1 2001
         * [DateFormat.MONTHYEARLONG] -> January 2001
         * [DateFormat.MONTHYEARSHORT] -> Jan 2001
         * [DateFormat.FULLLONG] -> Monday, 1 January 2001
         * [DateFormat.FULLSHORT] -> Monday, 1 Jan 2001
         * [DateFormat.YYYYMMDD] -> 20010101
         * [DateFormat.ISO] -> 2001-01-01
         */
        return when(format) {
            DateFormat.DAYMONTHYEARLONG -> "${DTUtils.monthIntToStr(dateMonth)} $dateDay $dateYear"
            DateFormat.DAYMONTHYEARSHORT -> "${DTUtils.monthIntToStr(dateMonth, short = true)} $dateDay $dateYear"
            DateFormat.MONTHYEARLONG -> "${DTUtils.monthIntToStr(dateMonth)} $dateYear"
            DateFormat.MONTHYEARSHORT -> "${DTUtils.monthIntToStr(dateMonth, short = true)} $dateYear"
            DateFormat.FULLLONG -> "${DTUtils.weekDayIntToStr(getDayOfWeek())}, $dateDay ${DTUtils.monthIntToStr(dateMonth)} $dateYear"
            DateFormat.FULLSHORT -> "${DTUtils.weekDayIntToStr(getDayOfWeek())}, $dateDay ${DTUtils.monthIntToStr(dateMonth, short = true)} $dateYear"
            DateFormat.YYYYMMDD -> "${getYear()}${getMonthOfYear().toString().padStart(2, '0')}${getDayOfMonth().toString().padStart(2, '0')}"
            DateFormat.ISO -> dateObject.toString()
        }
    }

    override fun toString(): String {
        return formatAsString(DateFormat.ISO)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Date) return false

        return (other.getDayOfMonth() == dateDay && other.getMonthOfYear() == dateMonth && other.getYear() == dateYear)
    }

    override fun hashCode(): Int {
        var result = dateYear
        result = 31 * result + dateMonth
        result = 31 * result + dateDay
        return result
    }
}

class DTUtils {
    companion object {
        // get current Date in system time timezone
        fun getNow(): Date{
            val currentMoment: Instant = Clock.System.now()
            val dateTZ: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            return Date(dateTZ.year, dateTZ.monthNumber, dateTZ.dayOfMonth)
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

        // Given a string formatted as "YYYYMMDD", return integer array [Y, M, D]
        fun parseDateStringToDate(dateString: String): Date {
            return Date(dateString.slice(0..3).toInt(), dateString.slice(4..5).toInt(), dateString.slice(6..7).toInt())
        }
    }
}

