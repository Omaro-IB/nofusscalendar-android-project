/*
 * ICSUtils
 * Use ICSUtils companion methods to read and parse a .ics file / write from VEvent to .ics
 * The plaintext is parsed into a VEvent object, which can return property values
 */

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

data class Value(val label: String, val word: String) {
    /**
     * Represents a single value (ex. FREQ=MONTHLY)
     */
    override fun toString(): String {
        return "$label=$word"
    }
}

data class Property(val label: String, val values: Array<Value>) {
    /**
     * Represents a property, which can contain 1 or more values
     * Ex. 1 value) SUMMARY:A birthday
     * Ex. > 1 value) RRULE:FREQ=MONTHLY;INTERVAL=1
     */
    fun valueLabelToIndex(label: String): Int {
        var index = 0
        for (value in values) {
            if (value.label.contains(label, ignoreCase = true)) { return index }
            index += 1
        }
        return -1
    }

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
}

data class VAlarm(val days: Int, val hours: Int, val minutes: Int, val seconds: Int) {
    /**
     * Represents a VAlarm, which specifies how long before the event the user should be notified
     */
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
    /**
     * Main VEvent object
     * Contains multiple properties and optionally a VAlarm
     */
    val uidIndex = propertyLabelToIndex("UID")  // store UID index since this accessed frequently

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

    fun isSingletonProperty(label: String): Boolean? {
        val propertyIndex = propertyLabelToIndex(label)
        if (propertyIndex == -1) { return null }
        return properties[propertyIndex].values.size == 1
    }

    fun getUID(): String {  // this function should be called when retrieving UID (much faster than getPropertyValue("UID"))
        if (uidIndex == -1) {return ""}
        return properties[uidIndex].values[0].word
    }

    fun getPropertyValue(propertyLabel: String): String? {
        val isSingleton: Boolean = isSingletonProperty(propertyLabel)?: return null
        if (!isSingleton) { throw IllegalArgumentException("$propertyLabel is not a singleton property but value label was not provided") }

        val propertyIndex = propertyLabelToIndex(propertyLabel)
        if (propertyIndex == -1) { return null }
        return properties[propertyIndex].values[0].word
    }

    fun getPropertyValue(propertyLabel: String, valueLabel: String): String? {
        val isSingleton: Boolean = isSingletonProperty(propertyLabel)?: return null
        if (isSingleton) { throw IllegalArgumentException("$propertyLabel is a singleton property but value label was provided") }

        val propertyIndex = propertyLabelToIndex(propertyLabel)
        if (propertyIndex == -1) { return null }

        val valueIndex = properties[propertyIndex].valueLabelToIndex(valueLabel)
        if (valueIndex == -1) { return null }
        return properties[propertyIndex].values[valueIndex].word
    }

    fun getPropertyIndexValue(propertyIndex: Int, valueLabel: String): String? {
        val valueIndex = properties[propertyIndex].valueLabelToIndex(valueLabel)
        if (valueIndex == -1) { return null }
        return properties[propertyIndex].values[valueIndex].word
    }


}

class ICSUtils{
    companion object{
        fun createVEvent(uid: String?, summary: String, location: String, description: String, color: String, startDate: Date, endDate: Date, startHour: Int?, startMinute: Int?, endHour: Int?, endMinute: Int?, rrule: RRule?): VEvent {
            /*
            Manually create a VEvent object, useful for adding an event from user input
            If UID not specified, the function will create one
             */
            val summaryProp = Property("SUMMARY", arrayOf(Value("", summary)))
            val locationProp = Property("LOCATION", arrayOf(Value("", location)))
            val descriptionProp = Property("DESCRIPTION", arrayOf(Value("", description)))
            val uidProp: Property
            if (uid == null) {
                uidProp = Property("UID", arrayOf(Value("", UUID.randomUUID().toString())))
            } else {
                uidProp = Property("UID", arrayOf(Value("", uid)))
            }
            val colorProp = Property("COLOR", arrayOf(Value("", color)))
            val dtstartProp: Property
            if (startHour == null && startMinute == null) {
                dtstartProp = Property("DTSTART", arrayOf(Value("", startDate.formatAsString(DateFormat.YYYYMMDD))))
            } else {
                dtstartProp = Property("DTSTART", arrayOf(Value("", "${startDate.formatAsString(DateFormat.YYYYMMDD)}T${startHour.toString().padStart(2,'0')}${startMinute.toString().padStart(2,'0')}00")))
            }
            val dtendProp: Property
            if (endHour == null && endMinute == null) {
                dtendProp = Property("DTEND", arrayOf(Value("", endDate.formatAsString(DateFormat.YYYYMMDD))))
            } else {
                dtendProp = Property("DTEND", arrayOf(Value("", "${endDate.formatAsString(DateFormat.YYYYMMDD)}T${endHour.toString().padStart(2,'0')}${endMinute.toString().padStart(2,'0')}00")))
            }
            if (rrule == null) {
                return VEvent(arrayOf(uidProp, summaryProp, locationProp, descriptionProp, colorProp, dtstartProp, dtendProp), null)
            } else {
                var rrulePropVals = arrayOf(
                    Value("FREQ", rrule.frequency.toString()),
                    Value("INTERVAL", rrule.interval.toString()),
                )
                if (rrule.byWhat != null && rrule.byVal != null) {rrulePropVals += Value("BY${rrule.byWhat}", rrule.byVal)}
                if (rrule.untilVal != null) {
                    if (rrule.untilWhat == UntilWhat.OCCURRENCES) { rrulePropVals += Value("COUNT", rrule.untilVal) }
                    if (rrule.untilWhat == UntilWhat.DATE) { rrulePropVals += Value("UNTIL", rrule.untilVal) }
                }
                return VEvent(arrayOf(uidProp, summaryProp, locationProp, descriptionProp, colorProp, dtstartProp, dtendProp, Property("RRULE", rrulePropVals)), null)
            }
        }

        fun parseICS(ics: String): Array<VEvent> {
            /*
            Given a .ics format string, parse into an array of VEvent objects
             */
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

        // Given an array of VEvent objects, create a .ics formatted string
        fun createICS(vevents: Array<VEvent>): String {
            var string = """
                BEGIN:VCALENDAR
                PRODID:-//omaribrah.im//NONSGML No Fuss Calendar//EN
                VERSION:1.0
            """.trimIndent()
            for (vevent in vevents) {if (vevent.uidIndex != -1) {string += "\n$vevent"}}
            string += "\nEND:VCALENDAR"
            return string
        }

        fun writeVEventsToUri(context: Context, uriS: String, vevents: Array<VEvent>) {
            val icsString = createICS(vevents)
            writeTextToUri(context, uriS, icsString)
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

