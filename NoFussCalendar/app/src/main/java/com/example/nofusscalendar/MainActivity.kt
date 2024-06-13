package com.example.nofusscalendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme
import android.widget.CalendarView
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoFussCalendarTheme {
                CalendarApp()
            }
        }
    }
}

@Composable
fun Calendar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface (color = colorResource(R.color.beige)) { Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier.padding(16.dp),
            factory = { CalendarView(context) },
            update = { calendarView ->
                // You can set various properties on the CalendarView here
                calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    // Handle date change
                }
            }
        )
    }}
}

@Preview
@Composable
fun CalendarApp() {
    Column {
        Spacer(modifier= Modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(colorResource(R.color.blackshadow)))
        Box(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.pinkbeige)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Fuss Calendar", fontWeight = FontWeight.Bold)
        }
        Calendar(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center))
    }
}