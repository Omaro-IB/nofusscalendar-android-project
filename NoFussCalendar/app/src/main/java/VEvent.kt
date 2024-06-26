import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class VEvent(val title: String)

// TODO: implement icalendar-kotlin to parse ICS string -> VEvent object
class VEventUtils{
    companion object{
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