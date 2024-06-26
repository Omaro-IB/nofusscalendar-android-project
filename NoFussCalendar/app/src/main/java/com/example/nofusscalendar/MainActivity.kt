package com.example.nofusscalendar
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoFussCalendarTheme {
                FileSelector()   // dialog for selecting a file
            }
        }
    }
}


@Composable
fun FileSelector() {
    // Launcher for file URI selector
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
        }
    )

    Column(modifier = Modifier.fillMaxHeight().fillMaxWidth().background(colorResource(R.color.greydef)).padding(horizontal = 20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        // Explanation text
        Text("For this app to work correctly, a file to must be selected to store app-data",
            color = colorResource(R.color.whitehighlight), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(10.dp))
        Text("You may export an .ics file from a calendar provider you already use, or " +
                "just use an empty plaintext file to begin",
            color = colorResource(R.color.silver_css3), textAlign = TextAlign.Center, fontSize = 15.sp, lineHeight = 15.sp)
        Spacer(modifier = Modifier.height(20.dp))

        // Select file button
        Button(onClick = { launcher.launch("*/*") }){Text("Select File")}
        selectedFileUri?.let { uri ->
            val uriS = uri.toString()
            val intent = Intent(context, Calendar::class.java).apply {
                putExtra("uri", uriS)
            }
            context.startActivity(intent)
        }
    }
}


