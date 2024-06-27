package com.example.nofusscalendar
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoFussCalendarTheme {
                MainScreen()   // dialog for selecting a file
            }
        }
    }
}


@Composable
fun MainScreen() {
    // State variables
    val context = LocalContext.current
    var writeUri: Boolean by remember { mutableStateOf(false) }

    // Open file from URI, set fileContent, and write URI to internal storage
    val startCalendar = { uri: Uri? ->
        // Take persistable permissions
        uri?.let {
            try {
                takePersistableUriPermission(context, it)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        try {
            // Write to internal storage for future use
            if (writeUri) {
                val fos: FileOutputStream =
                    context.openFileOutput("URILocation.txt", Context.MODE_PRIVATE)
                fos.write(uri.toString().toByteArray()); fos.flush(); fos.close()
                Log.d("MainActivity", "Write URILocation.txt - success")
            }

            // Start Calendar activity
            val intent = Intent(context, Calendar::class.java).apply { putExtra("uri", uri.toString()) }
            context.startActivity(intent)
        } catch (e: IOException) {
            // Error writing to URILocation.txt
            Log.d("MainActivity", "Write URILocation.txt - failure")
            e.printStackTrace()
        }
    }

    // Launch file selector
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? -> startCalendar(uri) }
    )

    // Begin app
    try {
        // Try reading URILocation.txt
        val fin: FileInputStream = context.openFileInput("URILocation.txt")
        Log.d("MainActivity", "Read URILocation.txt - success")
        var a: Int; val temp = StringBuilder(); while (fin.read().also { a = it } != -1) { temp.append(a.toChar()) }
        val uriS = temp.toString()  // URI String
        fin.close()
        if (uriS == "null") {throw IOException("Stored URILocation corrupted")}
        val uri: Uri = Uri.parse(uriS) // URI URI Object
        Log.d("READ URI AS!", uriS)
        startCalendar(uri)  // Start Calendar with URI!
    } catch (e: IOException) {
        // URILocation.txt not initialized
        writeUri = true

        Column(modifier = Modifier.fillMaxHeight().fillMaxWidth().background(colorResource(R.color.greydef)).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            // Explanation text
            Text("For this app to work correctly, a file must be selected to store app-data",
                color = colorResource(R.color.whitehighlight), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
            Text("You may export an .ics file from a calendar provider you already use, or " +
                    "just use an empty plaintext file to begin",
                color = colorResource(R.color.silver_css3), textAlign = TextAlign.Center, fontSize = 15.sp, lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // Select file button
            Button(onClick = {launcher.launch(arrayOf("*/*"))}) {
                Text(text = "Select File")
            }
        }
    }
}


// Grant persistable permissions to URI
fun takePersistableUriPermission(context: Context, uri: Uri) {
    val contentResolver: ContentResolver = context.contentResolver
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    // Check if the URI can be granted persistable permissions
    val takeFlagsSupported = DocumentsContract.isDocumentUri(context, uri)
    if (takeFlagsSupported) {
        try {
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    } else {
        // Handle the case where the URI does not support persistable permissions
        Log.d("MainActivity","Persistable permissions not supported for this URI: $uri")
    }
}

