/*
 * EventLookup
 * Use Event object to store event information (to be displayed in Events component)
 * Use EventLookup class to store a lookup table of events
 *     This lookup table can search for all events in a given month in O(log n) and return a hash
 *     map of day of month -> all events in this day
 */

class InvalidEventException(msg: String): Exception(msg)

enum class Frequency {YEARLY, MONTHLY, WEEKLY, DAILY}
enum class ByWhat {MONTH, DAY}
enum class UntilWhat {DATE, OCCURRENCES}

// RRule; specifies reoccurring event rule
data class RRule(val frequency: Frequency, val interval: Int = 1, val byWhat: ByWhat?, val byVal: String?, val untilWhat: UntilWhat?, val untilVal: String?)

data class Event(val title: String, val color: String, val description: String, val location: String, val startTime: String, val endTime: String,
                 val startDate: Date, val endDate: Date, val rrule: RRule?): Comparable<Event> {
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
                    finalDate_.changeDate(timeUnit_, (rrule.untilVal?: throw InvalidEventException("COUNT (occurrences) specified but no occurrences")).toInt())
                    finalDate_
                }
            }
    }

    fun getAllOccurrencesInMonth(year: Int, month: Int): Array<Int> {
        /**
         * Given a [year] and [month], return all days as [Array<Int>] this event occurs in
         */
        TODO("Not yet implemented")
        // TODO: get all occurrences within a given month using rrules (return array of days in month as integer)
        //          set currentDate = startDate
        //          initialize days array
        //          while currentDate is (before or = finalDate && before start of next month):
        //              if currentDate is in the month: add currentDate to days array
        //              increase currentDate using increase date methods and rrule frequency/interval
        //                  NOTE: make sure date actually exists in this year and month. If not, keep going back 1 day until it does
    }

    override fun compareTo(other: Event): Int {
        TODO("Not yet implemented")
        // TODO: return other event's final date - this event's final date (difference in days)
        //       NOTE: finalDate = null  means no final date
    }
}



class EventLookup {
    /**
     * Creates EventLookup class that very quickly finds what events should be displayed in a month
     */
    private var lookupTable: Array<Event> = arrayOf()

    fun lookup(year: Int, month: Int): HashMap<Int, Array<Event>> {
        /**
         * Lookup all events within a given [year] and [month]
         * Return a hash map; keys are days of month as integers
         *                    values are [Array<Event>] of all events in lookup table that appear in this day of month/year
         */
        TODO()
        // TODO: lookup from lookupTable by
        //          1) initialize empty hash map
        //          2) use binary search to find first event with finalDate after/on first of this month
        //          3) for all events that come after (+including said event):
        //              * for all days in event.getAllDaysInMonth(year, month):
        //              *   add to hash map: key = day, value = the event
    }

    fun createFromVevents(vevents: Array<VEvent>) {
        /**
         * Initialize [lookupTable] using [vevents] (an array of [VEvent] objects), which can be
         * easily parsed from an ICS file using [ICSUtils]
         */
        // Process all events into lookup table
        for (event in vevents) {
            // Essential properties; with no date, there can be no event
            val dtstart = event.getPropertyValue("DTSTART")?: continue
            val dtend = event.getPropertyValue("DTEND")?: continue
            val dtstartParsed = DTUtils.parseDateStringToDate(dtstart)
            val dtendParsed = DTUtils.parseDateStringToDate(dtend)

            // Start and end time
            val startTime: String
            val endTime: String
            val startDate: Date
            val endDate: Date
            if (!dtstart.contains('T', ignoreCase = true) || !dtend.contains('T', ignoreCase = true)) {  // event is allDay
                startTime = "all-day"
                endTime = ""
                startDate = dtstartParsed
                endDate = dtstartParsed
            } else {  // event is not all day
                startTime = "${dtstart.slice(9..10)}:${dtstart.slice(11..12)}"
                endTime = "${dtend.slice(9..10)}:${dtend.slice(11..12)}"
                startDate = dtstartParsed
                endDate = dtendParsed
            }

            // Other properties
            val title = event.getPropertyValue("SUMMARY")?: "No Title"
            val location = event.getPropertyValue("LOCATION")?:  "No Location"
            val description = event.getPropertyValue("DESCRIPTION")?: "No Description"
            val color = event.getPropertyValue("COLOR")?: "No Color"

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
            lookupTable += Event(title, color, description, location, startTime, endTime, startDate, endDate, rrule)
        }
        // TODO: sort lookupTable (Event should be comparable)
    }
}
