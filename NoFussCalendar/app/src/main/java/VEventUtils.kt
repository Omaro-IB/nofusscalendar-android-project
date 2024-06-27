
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

        // Given an array of VEvent objects, create a hash map of format:
        //  "YYYYMMDD" -> Array<Array<String>>[[title, location, description, color, allDay, start, end]]
        fun createEventHashMap(vevents: Array<VEvent>): HashMap<String, Array<Array<String>>> {
            var eventHashMap = HashMap<String, Array<Array<String>>>()
            var currArray1: Array<String> // base array, includes all common info for a given event (i.e. excluding time-relevant info)
            var currArray2: Array<String> // secondary array, includes allDay, start, and end

            vevents.forEach {
                // Create common base array
                currArray1 = arrayOf()
                currArray1 += try { it.getPropertyValue("SUMMARY") } catch(e: EventPropertyNotFoundException) { "No Title" }
                currArray1 += try { it.getPropertyValue("LOCATION") } catch(e: EventPropertyNotFoundException) { "No Location" }
                currArray1 += try { it.getPropertyValue("DESCRIPTION") } catch(e: EventPropertyNotFoundException) { "No Description" }
                currArray1 += try { it.getPropertyValue("COLOR") } catch(e: EventPropertyNotFoundException) { "No Color" }

                // For each day in event range, create full array and add to hash map
                // TODO:
                //  if 'T' not in start or end date, allDay = yes
                //  if allDay = yes
                //    for all days between start and end (not including end)
                //      currArray2 = currArray1
                //      currArray2 += "yes"
                //      currArray2 += ""
                //      currArray2 += ""
                //      add currArray2 to eventHashMap[day] (create new 2d array if necessary)
                //  if allDay = no
                //    for all days between start and end (including end)
                //      currArray2 = currArray1
                //      currArray2 += "no"
                //      currArray2 += formatted DTSTART time past 'T' till 'Z" if first day, else formatted DTSTART date up to 'T'
                //      currArray2 += formatted DTEND time past 'T' till 'Z' if last day, else formatted DTEND date up to 'T'
                //      add currArray2 to eventHashMap[day] (create new 2d array if necessary)

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
        SUMMARY:test repeating
        UID:0bef79ee0bbb42889231bfc83995e2da1719308622568
        X-SMT-CATEGORY-COLOR:-16746133
        CATEGORIES:Regular event
        LAST-MODIFIED:20240625T094342Z
        TRANSP:OPAQUE
        LOCATION:a location
        DTSTART:20240625T100000Z
        DTEND:20240625T100000Z
        X-SMT-MISSING-YEAR:0
        DTSTAMP:20240625T095211Z
        STATUS:CONFIRMED
        RRULE:FREQ=MONTHLY;INTERVAL=1
        DESCRIPTION:a description
        END:VEVENT
        BEGIN:VEVENT
        SUMMARY:test multiple days
        UID:1eec240ca8974e06a5697e0cfb5e64fd1719308647938
        X-SMT-CATEGORY-COLOR:-16746133
        CATEGORIES:Regular event
        LAST-MODIFIED:20240625T094407Z
        TRANSP:OPAQUE
        LOCATION:a location
        DTSTART:20240625T100000Z
        DTEND:20240628T100000Z
        X-SMT-MISSING-YEAR:0
        DTSTAMP:20240625T095211Z
        STATUS:CONFIRMED
        DESCRIPTION:a description
        END:VEVENT
        BEGIN:VEVENT
        SUMMARY:test color
        UID:a55860012b314f3dbeb637cd98c497f41719308674366
        X-SMT-CATEGORY-COLOR:-16746133
        CATEGORIES:Regular event
        LAST-MODIFIED:20240625T094434Z
        TRANSP:OPAQUE
        LOCATION:a location
        DTSTART:20240625T100000Z
        DTEND:20240625T100000Z
        X-SMT-MISSING-YEAR:0
        DTSTAMP:20240625T095211Z
        STATUS:CONFIRMED
        DESCRIPTION:a description
        END:VEVENT
        BEGIN:VEVENT
        SUMMARY:test alarm
        UID:a4036f53a80b4b609d71f4f2cd3682f01719309102774
        X-SMT-CATEGORY-COLOR:-16746133
        CATEGORIES:Regular event
        LAST-MODIFIED:20240625T095142Z
        TRANSP:OPAQUE
        LOCATION:a location
        DTSTART:20240625T120000Z
        DTEND:20240625T120000Z
        X-SMT-MISSING-YEAR:0
        DTSTAMP:20240625T095211Z
        STATUS:CONFIRMED
        DESCRIPTION:a description
        BEGIN:VALARM
        DESCRIPTION:Reminder
        ACTION:DISPLAY
        TRIGGER:-P0DT0H10M0S
        END:VALARM
        END:VEVENT
        END:VCALENDAR
    """.trimIndent()
    val parsed = VEventUtils.parseICS(ICSTest)
    println(parsed[0].getPropertyValue("dte"))
//    println(parsed[0].getPropertyValue("RRULE", "X"))
}