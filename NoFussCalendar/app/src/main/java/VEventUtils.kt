
import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class EventPropertyNotFoundException(m: String) : Exception (m)

data class Value(val label: String, val word: String) {
    override fun toString(): String {
        return "$label=$word"
    }
}

data class Property(val label: String, val values: Array<Value>) {
    override fun toString(): String {
        if (values.size == 1) { return "$label:${values[0].word}" }
        else {
            var string = "$label:"
            for (value in values) {
                string += "$value;"
            }
            return string.slice(0..<(string.length-1))
        }
    }

    fun valueLabelToIndex(label: String): Int {
        var index = 0
        for (value in values) {
            if (value.label.contains(label, ignoreCase = true)) { return index }
            index += 1
        }
        return -1
    }
}

data class VAlarm(val days: Int, val hours: Int, val minutes: Int, val seconds: Int) {
    override fun toString(): String {
        return """
            BEGIN:VALARM
            DESCRIPTION:Reminder
            ACTION:DISPLAY
            TRIGGER:-P${days}DT${hours}H${minutes}M${seconds}S
            END:VALARM
        """.trimIndent()
    }
}

data class VEvent(val properties: Array<Property>, val valarm: VAlarm?) {
    override fun toString(): String {
        var string = "BEGIN:VEVENT"
        for (property in properties) {string += "\n$property"}
        valarm?.let { string += "\n$it" }
        string += "\nEND:VEVENT"
        return string
    }

    fun propertyLabelToIndex(label: String): Int {
        var index = 0
        for (property in properties) {
            if (property.label.contains(label, ignoreCase = true)) { return index }
            index += 1
        }
        return -1
    }

    fun isSingletonProperty(label: String): Boolean {
        val propertyIndex = propertyLabelToIndex(label)
        if (propertyIndex == -1) { throw EventPropertyNotFoundException("$label property not found in event") }
        return properties[propertyIndex].values.size == 1
    }

    fun getPropertyValue(propertyLabel: String): String {
        if (! isSingletonProperty(propertyLabel)) { throw IllegalArgumentException("$propertyLabel is not a singleton property but value label was not provided") }
        val propertyIndex = propertyLabelToIndex(propertyLabel)
        if (propertyIndex == -1) { throw EventPropertyNotFoundException("$propertyLabel property not found in event") }

        return properties[propertyIndex].values[0].word
    }

    fun getPropertyValue(propertyLabel: String, valueLabel: String): String {
        if (isSingletonProperty(propertyLabel)) { throw IllegalArgumentException("$propertyLabel is a singleton property but value label was provided") }
        val propertyIndex = propertyLabelToIndex(propertyLabel)
        if (propertyIndex == -1) { throw EventPropertyNotFoundException("$propertyLabel property not found in event") }
        val valueIndex = properties[propertyIndex].valueLabelToIndex(valueLabel)
        if (valueIndex == -1) { throw EventPropertyNotFoundException("$valueLabel value not found in property $propertyLabel") }

        return properties[propertyIndex].values[valueIndex].word
    }


}

class VEventUtils{
    companion object{
        // Given a .ics format string, parse into an array of VEvent objects
        fun parseICS(ics: String): Array<VEvent> {
            val lines = ics.lines()
            var veventArray: Array<VEvent> = arrayOf()

            // Keep track of states as loop walks through string
            var currentProperties: Array<Property> = arrayOf()
            var currentValarm: VAlarm? = null
            var currentLine: List<String>
            var currentValues: Array<Value>
            var currentLabelWords: List<String>
            var currentLabelWord: List<String>
            var inBody = 0  // 0 = in VCALENDAR, 1 = in VEVENT, 2 = in VALARM

            lines.forEach {
                if (inBody == 0 && it == "BEGIN:VEVENT") {  // start of VEVENT
                    currentProperties = arrayOf()
                    currentValarm = null
                    inBody = 1
                } else if (inBody == 1 && it == "END:VEVENT") {  // end of VEVENT
                    veventArray += VEvent(currentProperties, currentValarm)
                    inBody = 0
                } else if (inBody == 1 && it == "BEGIN:VALARM") { // start of VALARM
                    inBody = 2
                } else if (inBody == 2 && it == "END:VALARM") {  // end of VALARM
                    inBody = 1
                } else if (inBody == 1) {  // middle of VEVENT
                    currentLine = it.split(":", limit = 2) // property
                    currentValues = arrayOf()
                    currentLabelWords = currentLine[1].split(";")
                    if (currentLabelWords.size == 1) {
                        currentValues = arrayOf(Value("", currentLabelWords[0])) // property with only 1 value
                    } else {
                        currentLabelWords.forEach {
                            currentLabelWord = it.split("=", limit = 2)
                            currentValues += Value(currentLabelWord[0], currentLabelWord[1])  // property with multiple values
                        }
                    }
                    currentProperties += Property(currentLine[0], currentValues)
                } else if (inBody == 2) {
                    currentLine = it.split(":", limit = 2)
                    if (currentLine[0] == "TRIGGER") {
                        var currentDays = ""; var currentHours = ""; var currentMinutes = ""; var currentSeconds = ""; var on = 0
                        val trigger = currentLine[1]
                        for (char in trigger) {
                            if (on == 0 && char == 'P') { on = 1 }
                            if (on == 1 && char == 'D') { on = 2 }
                            else if (on == 1) { currentDays += char }
                            else if (on == 2 && char == 'H') { on = 3 }
                            else if (on == 2) { currentHours += char }
                            else if (on == 3 && char == 'M') { on = 4 }
                            else if (on == 3) { currentMinutes += char }
                            else if (on == 4 && char == 'S') { on = 5 }
                            else if (on == 4) { currentSeconds += char }
                        }
                        currentDays = currentDays.drop(1)
                        currentHours = currentHours.drop(1)
                        currentValarm = VAlarm(currentDays.toInt(), currentHours.toInt(), currentMinutes.toInt(), currentSeconds.toInt())
                    }
                }
            }

            return veventArray
        }

        // Given a string formatted as "YYYYMMDD", return integer array [Y, M, D]
        fun parseDateStringToIntArray(dateString: String): Array<Int> {
            return arrayOf(dateString.slice(0..3).toInt(), dateString.slice(4..5).toInt(), dateString.slice(6..7).toInt())
        }

        // Given an array of VEvent objects, create a hash map of format:
        //  "YYYYMMDD" -> Array<Array<String>>[[title, location, description, color, allDay, start, end]]
        fun createEventHashMap(vevents: Array<VEvent>): HashMap<String, Array<Array<String>>> {
            val eventHashMap = HashMap<String, Array<Array<String>>>()
            var currArray1: Array<String> // base array, includes all common info for a given event (i.e. excluding time-relevant info)
            var currArray2: Array<String> // secondary array, includes allDay, start, and end
            var dtstart: String // start datetime for each event
            var dtend: String // end datetime for each event
            var dtstartParsed: Array<Int> // start datetime for each event in [Y,M,D]
            var dtendParsed: Array<Int> // end datetime for each event in [Y,M,D]

            for (event in vevents) {
                // Start and end date-times, this is the minimum for an event to exist so if the properties are not found, skip this event
                try {
                    dtstart = event.getPropertyValue("DTSTART")
                    dtend = event.getPropertyValue("DTSTART")
                    dtstartParsed = parseDateStringToIntArray(dtstart)
                    dtendParsed = parseDateStringToIntArray(dtend)
                } catch (e: EventPropertyNotFoundException) {continue}

                // Create common base array
                currArray1 = arrayOf()
                currArray1 += try { event.getPropertyValue("SUMMARY") } catch(e: EventPropertyNotFoundException) { "No Teventle" }
                currArray1 += try { event.getPropertyValue("LOCATION") } catch(e: EventPropertyNotFoundException) { "No Location" }
                currArray1 += try { event.getPropertyValue("DESCRIPTION") } catch(e: EventPropertyNotFoundException) { "No Description" }
                currArray1 += try { event.getPropertyValue("COLOR") } catch(e: EventPropertyNotFoundException) { "No Color" }

                val dayRange = DTUtils.getDaysBetween(dtstartParsed, dtendParsed)
                // For each day in event range, create full array and add to hash map
                if (!dtstart.contains('T', ignoreCase = true) || !dtend.contains('T', ignoreCase = true)) {  // event is allDay
//                    println("ALLDAY")
                    for (day in dayRange.indices) {
                        val y: Int = dayRange[day][0]; val m: Int = dayRange[day][1]; val d: Int = dayRange[day][2]
                        val ymd = "${y}${m.toString().padStart(2, '0')}${d.toString().padStart(2, '0')}"

                        currArray2 = currArray1
                        currArray2 += "yes"
                        currArray2 += ""
                        currArray2 += ""
                        // Add array to hash map
                        var x = eventHashMap[ymd]
                        if (x == null) {
                            eventHashMap[ymd] = arrayOf(currArray2)
//                            println("OK ALLDAY $currArray2")
                        } else {
                            x+= currArray2
                            eventHashMap[ymd] = x
                        }
                    }
                } else {  // event is not allDay
//                    println("NOTALLDAY from 0 to ${dayRange.size-1}")
                    for (day in dayRange.indices) {
                        val y: Int = dayRange[day][0]; val m: Int = dayRange[day][1]; val d: Int = dayRange[day][2]
                        val ymd = "${y}${m.toString().padStart(2, '0')}${d.toString().padStart(2, '0')}"

                        currArray2 = currArray1
                        currArray2 += "no"
                        currArray2 += if (day == 0) { // first day of event - display start time (hours: minutes)
                            DTUtils.timeToStr(dtstart.slice(9..10).toInt(), dtstart.slice(11..12).toInt())
                        } else {  // not first day of event - display start day (month/day)
                            "${dtstartParsed[1]}/${dtstartParsed[2]}"
                        }
                        currArray2 += if (day == dayRange.size-1) {  // last day of event - display end time (hours: minutes)
                            DTUtils.timeToStr(dtend.slice(9..10).toInt(), dtend.slice(11..12).toInt())
                        } else {    // not last day of event - display end day (month/day)
                            "${dtendParsed[1]}/${dtendParsed[2]}"
                        }
//                        println("$currArray2")
                        // Add array to hash map
                        var x = eventHashMap[ymd]
                        if (x == null) {
                            eventHashMap[ymd] = arrayOf(currArray2)
//                            println("OK NOTALLDAY $currArray2")
                        } else {
                            x+= currArray2
                            eventHashMap[ymd] = x
                        }
                    }
                }

                // TODO: change this to process vevents into a new class that does lookup in a more intelligent way
                //       previous implementation + pseudocode backed up in project resources folder -> previous vevent array processor.txt

            }
            return eventHashMap
        }
            


        // Given an array of VEvent objects, create a .ics formatted string
        fun createICS(vevents: Array<VEvent>): String {
            var string = """
                BEGIN:VCALENDAR
                PRODID:-//omaribrah.im//NONSGML No Fuss Calendar//EN
                VERSION:1.0
            """.trimIndent()
            for (vevent in vevents) {string += "\n$vevent"}
            string += "\nEND:VCALENDAR"
            return string
        }

        // Slice string to maximum given length, replace end with replaceWith
        fun clipString(string: String, replaceWith: String, limit: Int): String {
            return if (string.length > limit) {
                string.slice(0..<limit) + replaceWith
            } else {
                string
            }
        }

        // Takes file URI -> returns nullable string of content
        fun readTextFromUri(context: Context, uriS: String): String? {
            val uri: Uri = Uri.parse(uriS)
            return context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        }

        // Takes file URI & text -> write to file
        fun writeTextToUri(context: Context, uriS: String, text: String) {
            val uri: Uri = Uri.parse(uriS)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(text)
                }
            }
        }
    }
}

fun main() {
    val ICSTest = """
        BEGIN:VCALENDAR
        PRODID:-//Simple Mobile Tools//NONSGML Event Calendar//EN
        VERSION:2.0
        BEGIN:VEVENT
        SUMMARY:Canada Flight
        UID:2c10be6923a6429dbec43e70f6e5631a1718624251173
        X-SMT-CATEGORY-COLOR:-8219500
        CATEGORIES:Regular event
        LAST-MODIFIED:20240617T113731Z
        TRANSP:OPAQUE
        DTSTART:20240827T235000Z
        DTEND:20240827T235000Z
        X-SMT-MISSING-YEAR:0
        DTSTAMP:20240627T200208Z
        STATUS:CONFIRMED
        BEGIN:VALARM
        DESCRIPTION:Reminder
        ACTION:DISPLAY
        TRIGGER:-P0DT0H10M0S
        END:VALARM
        END:VEVENT
        END:VCALENDAR
    """.trimIndent()
    val parsed = VEventUtils.parseICS(ICSTest)
    val hashMap = VEventUtils.createEventHashMap(parsed)
    val y: Array<Array<String>> = hashMap["20240827"]?: arrayOf(arrayOf())
    println(hashMap.keys.first())
    println(y.size)
    y[0].forEach {
        println(it)
    }
}
