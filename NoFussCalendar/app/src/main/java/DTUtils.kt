import kotlinx.datetime.*

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

        fun monthIntToStr(month: Int): String{
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
}
