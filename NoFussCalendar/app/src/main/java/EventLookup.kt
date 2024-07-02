/*
 * EventLookup
 * Use Event object to store event information (to be displayed in Events component)
 * Use EventLookup class to store a lookup table of events
 *     This lookup table can quickly search for all events in a given month and return a map of which events are on which days
 *     map of day of month -> all events in this day
 */

class InvalidEventException(msg: String): Exception(msg)

enum class Frequency {YEARLY, MONTHLY, WEEKLY, DAILY}
enum class ByWhat {MONTH, DAY}
enum class UntilWhat {DATE, OCCURRENCES}

// RRule; specifies reoccurring event rule
data class RRule(val frequency: Frequency, val interval: Int = 1, val byWhat: ByWhat?, val byVal: String?, val untilWhat: UntilWhat?, val untilVal: String?) {
    override fun toString(): String {
        val freq = when(frequency) {
            Frequency.YEARLY -> "year"
            Frequency.MONTHLY -> "month"
            Frequency.WEEKLY -> "week"
            Frequency.DAILY -> "day"
        }

        val until = when(untilWhat) {
            UntilWhat.DATE -> " till ${DTUtils.parseDateStringToDate(untilVal?: throw InvalidEventException("UNTIL (date) specified but no date")).formatAsString(DateFormat.DAYMONTHYEARSHORT)}"
            UntilWhat.OCCURRENCES -> " until after $untilVal times"
            null -> ""
        }

        return when(interval) {
            1 -> "every $freq$until"
            else -> "every $interval ${freq}s$until"
        }
    }
}

data class Event(val uid: String, val title: String, val color: String, val description: String, val location: String,
                 val startHour: Int, val startMinute: Int, val endHour: Int, val endMinute: Int, val startDate: Date, val endDate: Date, val rrule: RRule?): Comparable<Event> {
    /**
     * Creates Event object given appropriate parameters
     */
    val finalDate: Date? =
        if (rrule == null) {endDate}  // no repeating rule, finalDate is end date
        else {
            when (rrule.untilWhat) {
                null -> {null}  // no limit, finalDate is infinity
                UntilWhat.DATE -> {  // date hard limit, finalDate is until date
                    DTUtils.parseDateStringToDate(rrule.untilVal?: throw InvalidEventException("UNTIL (date) specified but no date"))
                }
                UntilWhat.OCCURRENCES -> {  // x number of occurrences limit, finalDate is endDate + frequency * occurrences
                    val finalDate_ = endDate
                    val timeUnit_ = when(rrule.frequency) {Frequency.YEARLY -> TimeUnit.YEAR; Frequency.MONTHLY -> TimeUnit.MONTH; Frequency.WEEKLY -> TimeUnit.WEEK; Frequency.DAILY -> TimeUnit.DAY}
                    finalDate_.changeDate(timeUnit_, ((rrule.untilVal?: throw InvalidEventException("COUNT (occurrences) specified but no occurrences")).toInt() - 1) * (rrule.interval))
                    finalDate_
                }
            }
    }

    fun getAllOccurrencesInMonth(year: Int, month: Int): Array<Int> {
        /**
         * Given a [year] and [month], return all days as [Array<Int>] this event occurs in
         */
        var daysArray: Array<Int> = arrayOf()

        val currentDate = Date(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth())
        fun notPastFinal(): Boolean {
            return if (finalDate == null) { true }   // impossible to be past an infinite date
            else { !currentDate.isPastDate(finalDate) }
        }

        // Iterate from event start date till final date/next month (whichever is first)
        while (notPastFinal() && currentDate.isBeforeNextMonth(year, month)) {
            // add day of month if within the month we're checking + within date range
            if (currentDate.isInMonth(year, month)) {
                if (finalDate == null && currentDate.isOnOrPastDate(startDate)) {daysArray += currentDate.getDayOfMonth()}
                else if (finalDate != null && currentDate.isBetweenDates(startDate, finalDate)) {daysArray += currentDate.getDayOfMonth()}
            }

            if (rrule != null) {  // repeating event, increase currentDate according to rrule
                val timeUnit_ = when(rrule.frequency) {Frequency.YEARLY -> TimeUnit.YEAR; Frequency.MONTHLY -> TimeUnit.MONTH; Frequency.WEEKLY -> TimeUnit.WEEK; Frequency.DAILY -> TimeUnit.DAY}
                currentDate.changeDate(timeUnit_, rrule.interval)  // increase current date according to rrule
            } else {  // not-repeating event, increase by 1 day
                currentDate.changeDate(TimeUnit.DAY, 1)
            }
        }

        return daysArray
    }

    override fun compareTo(other: Event): Int {
        return if (finalDate == null && other.finalDate == null) {0}  // both final dates are infinity
        else if (finalDate == null) {Int.MAX_VALUE}  // this final date is infinity, other is limited
        else if (other.finalDate == null) {Int.MIN_VALUE} // this final date is limited, other is infinity
        else {finalDate.subtractDays(other.finalDate) }  // both final dates are limited
    }
}



class EventLookup {
    /**
     * Creates EventLookup class that very quickly finds what events should be displayed in a month
     */
    private var lookupTable: Array<Event> = arrayOf()

    fun lookup(year: Int, month: Int): Array<Pair<Array<Int>, Event>> {
        /**
         * Lookup all events within a given [year] and [month]
         * Return an array of pairs; first pair-value is an array of integers representing days of this month
         *                           second pair-value is an Event object for event appearing on all days in the integer array
         */
        var eventDayMap: Array<Pair<Array<Int>, Event>> = arrayOf()

        var addToMap = false
        val firstOfMonth = Date(year, month, 1)

        fun addToMap(event: Event) {  // add event to map if it has at least one occurrence in the month
            val allOccurrences = event.getAllOccurrencesInMonth(year, month)
            if (allOccurrences.isNotEmpty()) {eventDayMap += Pair(allOccurrences, event)}
        }

        lookupTable.forEach {
            if (addToMap) {
                addToMap(it)
            } else {
                if (it.finalDate == null) {addToMap(it); addToMap = true}
                else if (it.finalDate.isOnOrPastDate(firstOfMonth)) {addToMap(it); addToMap = true}
            }
        }

        return eventDayMap
    }

    fun createFromVevents(vevents: Array<VEvent>) {
        /**
         * Initialize [lookupTable] using [vevents] (an array of [VEvent] objects), which can be
         * easily parsed from an ICS file using [ICSUtils]
         */
        // Process all events into lookup table
        for (event in vevents) {
            // Essential properties; with no date or UID, there can be no event
            val uid = event.getPropertyValue("UID")?: continue
            val dtstart = event.getPropertyValue("DTSTART")?: continue
            val dtend = event.getPropertyValue("DTEND")?: continue
            val dtstartParsed = DTUtils.parseDateStringToDate(dtstart)
            val dtendParsed = DTUtils.parseDateStringToDate(dtend)

            // Start and end time
            val startHour: Int
            val startMinute: Int
            val endHour: Int
            val endMinute: Int
            val startDate: Date
            val endDate: Date
            if (!dtstart.contains('T', ignoreCase = true) || !dtend.contains('T', ignoreCase = true)) {  // event is allDay
                startHour = -1; startMinute = -1
                endHour = -1; endMinute = -1
                startDate = dtstartParsed
                endDate = dtendParsed
                endDate.changeDate(TimeUnit.DAY, -1)
            } else {  // event is not all day
                startHour = dtstart.slice(9..10).toInt(); startMinute = dtstart.slice(11..12).toInt()
                endHour = dtend.slice(9..10).toInt(); endMinute = dtend.slice(11..12).toInt()
                startDate = dtstartParsed
                endDate = dtendParsed
            }

            // Other properties
            val title = event.getPropertyValue("SUMMARY")?: "No Title"
            val location = event.getPropertyValue("LOCATION")?:  "No Location"
            val description = event.getPropertyValue("DESCRIPTION")?: ""
            val color = event.getPropertyValue("COLOR")?: ""

            // Repeating rule
            val rruleIndex = event.propertyLabelToIndex("RRULE")
            val rrule: RRule?

            if (rruleIndex == -1) {  //non-repeating
                rrule = null
            } else {  // repeating
                // START: CREATE RRULE
                //   frequency & interval
                val rruleFreq = Frequency.valueOf(event.getPropertyIndexValue(rruleIndex, "FREQ")?: "DAILY")
                val rruleInterval = event.getPropertyIndexValue(rruleIndex, "INTERVAL")?: "1"
                //   repeat by day/month and which day/month
                val rruleByWhat: ByWhat?
                val rruleByVal: String?
                var rruleByWhat_ = event.getPropertyIndexValue(rruleIndex, "BYDAY")
                if (rruleByWhat_ != null) {rruleByWhat = ByWhat.DAY; rruleByVal = rruleByWhat_}
                else {
                    rruleByWhat_ = event.getPropertyIndexValue(rruleIndex, "BYMONTH")
                    if (rruleByWhat_ != null) {rruleByWhat = ByWhat.MONTH; rruleByVal = rruleByWhat_}
                    else {rruleByWhat = null; rruleByVal = null}
                }
                //   repeat until a date/x # of occurrences and which/how many
                val rruleUntilWhat: UntilWhat?
                val rruleUntilVal: String?
                var rruleUntilWhat_ = event.getPropertyIndexValue(rruleIndex, "UNTIL")
                if (rruleUntilWhat_ != null) {rruleUntilWhat = UntilWhat.DATE; rruleUntilVal = rruleUntilWhat_}
                else {
                    rruleUntilWhat_ = event.getPropertyIndexValue(rruleIndex, "COUNT")
                    if (rruleUntilWhat_ != null) {rruleUntilWhat = UntilWhat.OCCURRENCES; rruleUntilVal = rruleUntilWhat_}
                    else {rruleUntilWhat = null; rruleUntilVal = null}
                }
                //    create rrule
                rrule = RRule(  // Created RRULE
                    frequency = rruleFreq,
                    interval = rruleInterval.toInt(),
                    byWhat =  rruleByWhat,
                    byVal = rruleByVal,
                    untilWhat = rruleUntilWhat,
                    untilVal = rruleUntilVal
                )
                // END: CREATE RRULE
            }

            // Add event to table
            lookupTable += Event(uid, title, color, description, location, startHour, startMinute, endHour, endMinute, startDate, endDate, rrule)
        }

        lookupTable.sort()  // sort by final date for binary search
    }
}
