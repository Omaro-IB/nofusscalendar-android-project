class InvalidEventException(msg: String): Exception(msg)

enum class Frequency {YEARLY, MONTHLY, WEEKLY, DAILY}
enum class ByWhat {MONTH, DAY}
enum class UntilWhat {DATE, OCCURRENCES}

data class Date(val year: Int, val month: Int, val day: Int)
// TODO: increase date methods
//          pass in frequency/interval
//              if `freq` = daily -> skip to (next )*`frequency` day
//              if `freq` = weekly -> skip to (next )*`frequency` same weekday
//              if `freq` = monthly -> skip to (next )*`frequency` same day of month
//              if `freq` = yearly -> skip to (next )*`frequency` same day of year

data class RRule(val frequency: Frequency, val interval: Int = 1, val byWhat: ByWhat?, val byVal: String?, val untilWhat: UntilWhat?, val untilVal: String?)

data class Event(val title: String, val color: String, val description: String, val location: String, val startTime: String, val endTime: String,
                 val startDate: Date, val endDate: Date, val rrule: RRule?): Comparable<Event> {
    val finalDate: Date? = if (rrule == null) {endDate} else {
        when (rrule.untilWhat) {
            null -> {null}  // no limit,
            UntilWhat.DATE -> {  // set date hard limit
                val finalDate_ = DTUtils.parseDateStringToIntArray(rrule.untilVal?: throw InvalidEventException("UNTIL (date) specified but no date"))
                Date(finalDate_[0], finalDate_[1], finalDate_[2])
            }
            else -> {  // x number of occurrences limit
                Date(1,1,1)  // TODO: endDate + rrule.frequency * rrule.untilVal
            }
        }
    }

    override fun compareTo(other: Event): Int {
        TODO("Not yet implemented")
        // TODO: return other event's final date - this event's final date (difference in days)
        //       NOTE: finalDate = null  means no final date
    }

    fun getAllDaysInMonth(year: Int, month: Int): Array<Int> {
        TODO("Not yet implemented")
        // TODO: get all dates within a given month using rrules
        //          set currentDate = startDate
        //          initialize days array
        //          while currentDate is (before or = finalDate && before start of next month):
        //              if currentDate is in the month: add currentDate to days array
        //              increase currentDate using increase date methods and rrule frequency/interval
        //                  NOTE: make sure date actually exists in this year and month. If not, keep going back 1 day until it does
    }
}



class EventLookup {
    private var lookupTable: Array<Event> = arrayOf()

    fun createFromVevents(vevents: Array<VEvent>) {
        // Process all events into lookup table
        for (event in vevents) {
            // Essential properties; with no date, there can be no event
            val dtstart = event.getPropertyValue("DTSTART")?: continue
            val dtend = event.getPropertyValue("DTEND")?: continue
            val dtstartParsed = DTUtils.parseDateStringToIntArray(dtstart)
            val dtendParsed = DTUtils.parseDateStringToIntArray(dtend)

            // Start and end time
            val startTime: String
            val endTime: String
            val startDate: Date
            val endDate: Date
            if (!dtstart.contains('T', ignoreCase = true) || !dtend.contains('T', ignoreCase = true)) {  // event is allDay
                startTime = "all-day"
                endTime = ""
                startDate = Date(dtstartParsed[0], dtstartParsed[1], dtstartParsed[2])
                endDate = Date(dtstartParsed[0], dtstartParsed[1], dtstartParsed[2])
            } else {
                startTime = "${dtstart.slice(9..10)}:${dtstart.slice(11..12)}"
                endTime = "${dtend.slice(9..10)}:${dtend.slice(11..12)}"
                startDate = Date(dtstartParsed[0], dtstartParsed[1], dtstartParsed[2])
                endDate = Date(dtendParsed[0], dtendParsed[1], dtendParsed[2])
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

    //
    fun lookup(year: Int, month: Int): HashMap<Int, Array<Event>> {
        TODO()
        // TODO: lookup from lookupTable by
        //          1) initialize empty hash map
        //          2) use binary search to find first event with finalDate after/on first of this month
        //          3) for all events that come after (+including said event):
        //              * for all days in event.getAllDaysInMonth(year, month):
        //              *   add to hash map: key = day, value = the event
    }
}
