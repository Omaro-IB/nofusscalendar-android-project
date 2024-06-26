package com.example.nofusscalendar

import DTUtils
import VEventUtils
import android.nfc.FormatException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme

class Calendar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uri = intent.getStringExtra("uri") ?: "No uri"

            NoFussCalendarTheme {
                Column {
                    // Dark space
                    Spacer(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .background(colorResource(R.color.beigedark))
                    )
                    // Calendar
                    Calendar(modifier = Modifier
                        .background(colorResource(R.color.beige))
                        .fillMaxWidth()
                        .fillMaxHeight()
                        , uri = uri)
                }
            }
        }
    }
}

@Composable
fun Calendar(modifier: Modifier = Modifier, uri: String) {
    // Displayed year/month
    var year: Int by remember { mutableStateOf(DTUtils.getYear()) }
    var month: Int by remember { mutableStateOf(DTUtils.getMonth()) }
    // Selected year/month/day
    var selectedYear: Int by remember { mutableStateOf(DTUtils.getYear()) }
    var selectedMonth: Int by remember { mutableStateOf(DTUtils.getMonth()) }
    var selectedDay: Int by remember { mutableStateOf(DTUtils.getDay()) }

    // Fix displayed month
    if (month > 12) {month = 1; year += 1 }
    if (month < 1) {month = 12; year -= 1}

    // Create event intent



    Column(modifier = modifier) {
        Column(modifier = Modifier.height(400.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Month changer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                // Left
                Box {
                    IconButton(onClick = { year-- }, modifier = Modifier.offset((-20).dp)){ Icon(
                        painterResource(R.drawable.chevron_double_left), contentDescription = "Go back 1 year") }
                    IconButton(onClick = { month-- }){ Icon(painterResource(R.drawable.chevron_left), contentDescription = "Go back 1 month") }
                }
                // Title
                Row(modifier = Modifier.width(200.dp), horizontalArrangement = Arrangement.Center) {Text(text = "${DTUtils.monthIntToStr(month)} $year", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)}
                // Right
                Box {
                    IconButton(onClick = { month++ }){ Icon(painterResource(R.drawable.chevron_right), contentDescription = "Go forward 1 month") }
                    IconButton(onClick = { year++ }, modifier = Modifier.offset(20.dp)){ Icon(
                        painterResource(R.drawable.chevron_double_right), contentDescription = "Go forward 1 year") }
                }
            }
            // Month grid - display starting on first of month
            MonthDays(startOn = DTUtils.dateToWeekDay(year, month, 1), numDays = DTUtils.getMonthDays(year, month),
                selectedDay = (if (year == selectedYear && month == selectedMonth) selectedDay else 0), onDaySelect = {day: Int -> selectedDay = day; selectedMonth = month; selectedYear = year}) // only show selected day if on selected year/month

        }

        // Bottom half:
        Spacer(modifier = Modifier
            .height(2.dp)
            .fillMaxWidth()
            .background(colorResource(R.color.blackshadow)))  // Divider
        // Events
        Box {
            // Event list
            Events(modifier = Modifier
                .background(colorResource(R.color.beige))
                .fillMaxWidth()
                .fillMaxHeight(), selectedYear, selectedMonth, selectedDay)
            // Add event button
            val iconSize = 96
            Box(modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.BottomEnd)) {
                IconButton(onClick = { /* TODO: Handle add event */ }, modifier = Modifier.size(iconSize.dp)){ Icon(
                    painterResource(R.drawable.plus_box), tint = colorResource(R.color.buttongreen), contentDescription = "Add event", modifier = Modifier.size((iconSize*0.85).dp)) }
            }
        }
    }
}


@Composable
fun Event(title: String, location: String, description: String, allDay: Boolean, color: String, start: String, end: String){
    val c = when(color){
        "black" -> colorResource(R.color.black_css3)
        "silver" -> colorResource(R.color.silver_css3)
        "gray" -> colorResource(R.color.gray_css3)
        "white" -> colorResource(R.color.white_css3)
        "maroon" -> colorResource(R.color.maroon_css3)
        "red" -> colorResource(R.color.red_css3)
        "purple" -> colorResource(R.color.purple_css3)
        "fuchsia" -> colorResource(R.color.fuchsia_css3)
        "green" -> colorResource(R.color.green_css3)
        "lime" -> colorResource(R.color.lime_css3)
        "olive" -> colorResource(R.color.olive_css3)
        "yellow" -> colorResource(R.color.yellow_css3)
        "navy" -> colorResource(R.color.navy_css3)
        "blue" -> colorResource(R.color.black_css3)
        "teal" -> colorResource(R.color.teal_css3)
        "aqua" -> colorResource(R.color.aqua_css3)
        else -> throw FormatException("color not available")
    }
    Row {
        // Color spacer
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Box(modifier = Modifier
            .height(40.dp)
            .width(6.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(c)
            .border(
                width = (0.4).dp,
                color = colorResource(R.color.blackshadow),
                shape = RoundedCornerShape(12.dp)
            ))
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        // Two lines
        Column {
            // Title, location, and start time
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {Text(VEventUtils.clipString(title, "..", 14), fontWeight = FontWeight.Bold); Text(" @${VEventUtils.clipString(location, "..", 14)}", fontStyle = FontStyle.Italic)}
                Text(if (allDay) "all-day  " else (start))
            }
            // Description and end time
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(VEventUtils.clipString(description, "...", 28), color = colorResource(R.color.gray_css3))
                Text(if (allDay) "" else (end))
            }
            Spacer(modifier = Modifier.padding(vertical = 2.dp))
            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .padding(end = 22.dp)
                .background(colorResource(R.color.pinkbeige)))
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun Events(modifier: Modifier = Modifier, year: Int, month: Int, day: Int) {
    // Title
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Events for ${DTUtils.weekDayIntToStr(DTUtils.dateToWeekDay(year, month, day))}, $day ${DTUtils.monthIntToStr(month)} $year:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(10.dp))
        // TODO: render all events using VEvent.kt
//      LazyColumn{
//          items(...) { event ->
//               ...
//          }
//      }
        // TODO: remove this (test)
        Column {
            Event("An Event or something or other", "Somewhereeeeeeeeeeeee", "Something descriptive", false, "aqua", "11:00 AM", "12:00 PM")
            Event("Go do something", "idk", "A description", false, "silver", "9:00 AM", "7:00 PM")
            Event("Another Event", "Somewhere else", "Something else descriptive because I want more and more words", true, "maroon", "11:00 AM", "12:00 PM")
        }
    }
}


@Composable
fun DaySelect(modifier: Modifier = Modifier, day: Int, selected: Boolean, onClick: () -> Unit) {
    // Highlight color when selected
    val c = when(selected){
        true -> colorResource(R.color.buttongreenhighlight)
        else -> colorResource(R.color.buttongreen)
    }

    // Day box
    Box {
        Button(onClick = onClick,
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
fun MonthDays(startOn: Int, numDays: Int, selectedDay: Int, onDaySelect: (Int) -> Unit) {
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
                else -> DaySelect(day = day - startOn + 2, selected = selectedDay == (day - startOn + 2), onClick = {onDaySelect(day - startOn + 2)})  // first day to last
            }
        }
    }
}

