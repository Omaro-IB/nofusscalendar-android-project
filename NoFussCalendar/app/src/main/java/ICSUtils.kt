
import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

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

    fun isSingletonProperty(label: String): Boolean? {
        val propertyIndex = propertyLabelToIndex(label)
        if (propertyIndex == -1) { return null }
        return properties[propertyIndex].values.size == 1
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

