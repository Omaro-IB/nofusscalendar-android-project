package com.example.nofusscalendar
import DTUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoFussCalendarTheme {
                Column {
                    // Dark space
                    Spacer(modifier= Modifier.height(60.dp).fillMaxWidth().background(colorResource(R.color.beigedark)))
                    // Calendar
                    Calendar(modifier = Modifier.background(colorResource(R.color.beige)).fillMaxWidth().height(400.dp))
                    // Light space
                    Spacer(modifier = Modifier.height(2.dp).fillMaxWidth().background(colorResource(R.color.black)))
                    // Event list -- TODO: Update day based on selected day
                    Events(modifier = Modifier.background(colorResource(R.color.beige)).fillMaxWidth().fillMaxHeight(), 2024, 6, 25)
                }
            }
        }
    }
}

@Composable
fun Events(modifier: Modifier = Modifier, year: Int, month: Int, day: Int) {
    // Title
    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Events for ${DTUtils.weekDayIntToStr(DTUtils.dateToWeekDay(year, month, day))}, $day ${DTUtils.monthIntToStr(month)} $year", fontWeight = FontWeight.Bold)
    }
    // TODO: render all events using VEvent.kt, icalendar-kotlin, and media store to read file
//    LazyColumn{
//        items(...) { event ->
//            ...
//        }
//    }
}


@Composable
fun DaySelect(modifier: Modifier = Modifier, day: Int, selected: Boolean) {
    // Highlight color when selected
    val c = when(selected){
        true -> colorResource(R.color.buttongreenhighlight)
        else -> colorResource(R.color.buttongreen)
    }

    // Day box
    Box {
        Button(onClick = { /* TODO: Update selected day */ },
            modifier = modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(5),
            colors = ButtonDefaults.buttonColors(containerColor = c)
        ){}
        Text(text = day.toString(), Modifier.align(Alignment.Center), color = colorResource(R.color.whitehighlight))
    }
}


@Composable
fun MonthDays(startOn: Int, numDays: Int, selectedDay: Int) {
    // Days of week
    LazyVerticalGrid(
        columns = GridCells.Fixed(7)
    ) {
        items(7) { day ->
            Text(text = DTUtils.weekDayIntToStr(day+1, true), textAlign = TextAlign.Center)
        }
    }

    // Divider line
    Divider(color = colorResource(R.color.blackshadow), thickness = 1.dp, modifier = Modifier
        .padding(top = 10.dp, bottom = 5.dp)
        .padding(horizontal = 12.dp))

    // Days of month, use DaySelect component
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        items(numDays+startOn-1) { day ->
            when(day+1) {
                in (1..<startOn) -> Spacer(modifier = Modifier.fillMaxWidth()) // leave gap till first day
                else -> DaySelect(day = day - startOn + 2, selected = selectedDay == (day - startOn + 2))  // first day to last
            }
        }
    }
}


@Composable
fun Calendar(modifier: Modifier = Modifier) {
    // Displayed year/month
    var year: Int by remember { mutableStateOf(DTUtils.getYear()) }
    var month: Int by remember { mutableStateOf(DTUtils.getMonth()) }
    // Selected year/month/day
    val selectedYear: Int by remember { mutableStateOf(DTUtils.getYear()) }
    val selectedMonth: Int by remember { mutableStateOf(DTUtils.getMonth()) }
    val selectedDay: Int by remember { mutableStateOf(DTUtils.getDay()) }

    // Fix displayed month
    if (month > 12) {month = 1; year += 1 }
    if (month < 1) {month = 12; year -= 1}

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Month changer
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            // Left
            Box {
                IconButton(onClick = { year-- }, modifier = Modifier.offset((-20).dp)){ Icon(painterResource(R.drawable.chevron_double_left), contentDescription = "Go back 1 year") }
                IconButton(onClick = { month-- }){ Icon(painterResource(R.drawable.chevron_left), contentDescription = "Go back 1 month") }
            }
            // Title
            Row(modifier = Modifier.width(200.dp), horizontalArrangement = Arrangement.Center) {Text(text = "${DTUtils.monthIntToStr(month)} $year", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)}
            // Right
            Box {
                IconButton(onClick = { month++ }){ Icon(painterResource(R.drawable.chevron_right), contentDescription = "Go forward 1 month") }
                IconButton(onClick = { year++ }, modifier = Modifier.offset(20.dp)){ Icon(painterResource(R.drawable.chevron_double_right), contentDescription = "Go forward 1 year") }
            }
        }
        // Month grid - display starting on first of month
        MonthDays(startOn = DTUtils.dateToWeekDay(year, month, 1), numDays = DTUtils.getMonthDays(year, month),
            selectedDay = (if (year == selectedYear && month == selectedMonth) selectedDay else 0)) // only show selected day if on selected year/month
    }
}