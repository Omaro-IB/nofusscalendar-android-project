package com.example.nofusscalendar

import DTUtils
import Date
import DateFormat
import Event
import EventLookup
import ICSUtils
import TimeUnit
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
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

        val uri = intent.getStringExtra("uri") ?: "No uri"
        val icsRaw = ICSUtils.readTextFromUri(this@Calendar, uri)?: ""

        setContent {
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
                        , icsRaw = icsRaw)
                }
            }
        }
    }
}

@Composable
fun Calendar(modifier: Modifier = Modifier, icsRaw: String) {
    // ICS Parsing and Events
    if (icsRaw == "") { Text("Error reading ICS file", color = colorResource(R.color.buttonred)) }
    val events = ICSUtils.parseICS(icsRaw)  // contains all events info, easy to parse between .ics string
    val eventLookup = EventLookup(); eventLookup.createFromVevents(events) // hash map for quick lookup & quick display
    MainScreen(modifier = modifier, eventLookup = eventLookup)
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, eventLookup: EventLookup) {
    val context = LocalContext.current
    // Displayed year/month
//    var year: Int by remember { mutableStateOf(DTUtils.getYear()) }
//    var month: Int by remember { mutableStateOf(DTUtils.getMonth()) }
//    // Selected year/month/day
//    var selectedYear: Int by remember { mutableStateOf(DTUtils.getYear()) }
//    var selectedMonth: Int by remember { mutableStateOf(DTUtils.getMonth()) }
//    var selectedDay: Int by remember { mutableStateOf(DTUtils.getDay()) }
//    // Fix displayed month
//    if (month > 12) {month = 1; year += 1 }
//    if (month < 1) {month = 12; year -= 1}

    // Displayed date
    var displayedDate: Date by remember { mutableStateOf(DTUtils.getNow()) }
    // Selected date
    var selectedDate: Date by remember { mutableStateOf(DTUtils.getNow()) }

    // eventsHashMap for this month from lookup
    val eventsHashMap = eventLookup.lookup(selectedDate.getYear(), selectedDate.getMonthOfYear())


    Column(modifier = modifier) {
        Column(modifier = Modifier.height(400.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Month changer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                // Left
                Box {
                    IconButton(onClick = { displayedDate.changeDate(TimeUnit.YEAR, -1) }, modifier = Modifier.offset((-20).dp)){ Icon(
                        painterResource(R.drawable.chevron_double_left), contentDescription = "Go back 1 year") }
                    IconButton(onClick = { displayedDate.changeDate(TimeUnit.MONTH, -1) }){ Icon(painterResource(R.drawable.chevron_left), contentDescription = "Go back 1 month") }
                }
                // Title
                Row(modifier = Modifier.width(200.dp), horizontalArrangement = Arrangement.Center) {Text(text = displayedDate.formatAsString(DateFormat.MONTHYEARLONG), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)}
                // Right
                Box {
                    IconButton(onClick = { displayedDate.changeDate(TimeUnit.MONTH, 1) }){ Icon(painterResource(R.drawable.chevron_right), contentDescription = "Go forward 1 month") }
                    IconButton(onClick = { displayedDate.changeDate(TimeUnit.YEAR, 1) }, modifier = Modifier.offset(20.dp)){ Icon(
                        painterResource(R.drawable.chevron_double_right), contentDescription = "Go forward 1 year") }
                }
            }
            // Month grid - display starting on first of month
            MonthDays(startOn = displayedDate.getDayOfWeekOfFirstOfMonth(), numDays = displayedDate.getNumDaysOfMonth(),
                selectedDay = (if (displayedDate.getYear() == selectedDate.getYear() && displayedDate.getMonthOfYear() == selectedDate.getMonthOfYear()) selectedDate.getDayOfMonth() else 0),
                onDaySelect = {day: Int -> selectedDate.setDayOfMonth(day); selectedDate.setMonthOfYear(displayedDate.getMonthOfYear()); selectedDate.setYear(displayedDate.getYear())}) // only show selected day if on selected year/month

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
                .fillMaxHeight(), selectedDate, eventsHashMap[selectedDate.getDayOfMonth()])
            // Add event button
            val iconSize = 96
            Box(modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.BottomEnd)) {
                IconButton(onClick = { val intent = Intent(context, NewEvent::class.java).apply { putExtra("selectedDate", selectedDate.formatAsString(DateFormat.YYYYMMDD)) } // Start New Event activity
                                       context.startActivity(intent) }
                    , modifier = Modifier.size(iconSize.dp)){ Icon(
                    painterResource(R.drawable.plus_box), tint = colorResource(R.color.buttongreen), contentDescription = "Add event", modifier = Modifier.size((iconSize*0.85).dp)) }
            }
        }
    }
}


@Composable
fun EventItem(title: String, location: String, description: String, color: String, start: String, end: String){
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
        else -> colorResource(R.color.beige)
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
                Row {Text(ICSUtils.clipString(title, "..", 14), fontWeight = FontWeight.Bold); Text(" @${ICSUtils.clipString(location, "..", 14)}", fontStyle = FontStyle.Italic)}
                Text(start)
            }
            // Description and end time
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ICSUtils.clipString(description, "...", 28), color = colorResource(R.color.gray_css3))
                Text(end)
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
fun Events(modifier: Modifier = Modifier, date: Date, eventArray: Array<Event>?) {
    // Title
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Events for ${date.formatAsString(DateFormat.FULLLONG)}:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(10.dp))

        if (eventArray == null) {
            Text("No events for this day")
        } else {
          LazyColumn{
              items(eventArray.size) { event ->
                  val startDate = eventArray[event].startDate
                  val endDate = eventArray[event].endDate
                  var startTime = eventArray[event].startTime
                  var endTime = eventArray[event].endTime
                  if (startDate != date) {startTime += " (${startDate.formatAsString(DateFormat.MONTHYEARSHORT)})"}  // this event does not start on selected day
                  if (endDate != date) {endTime += " (${endDate.formatAsString(DateFormat.MONTHYEARLONG)})"}  // this event does not end on selected day

                  EventItem(
                      title = eventArray[event].title,
                      location = eventArray[event].location,
                      description = eventArray[event].description,
                      color = eventArray[event].color,
                      start = startTime,
                      end = endTime
                  )
              }
          }
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


