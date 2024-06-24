package com.example.nofusscalendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme

class NewEvent : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoFussCalendarTheme {
                NewEventApp()
            }
        }
    }
}


@Preview
@Composable
fun NewEventApp(modifier: Modifier = Modifier) {
    Surface(color = colorResource(R.color.whitehighlight)) { Column(modifier = modifier) {} }
}