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
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val redraw = remember { mutableStateOf(0) }

    // Displayed date
    val displayedDate = remember { mutableStateOf(DTUtils.getNow()) }
    // Selected date
    val selectedDate = remember { mutableStateOf(DTUtils.getNow()) }

    // eventsDayMap for this month from lookup
    val eventDayMap = eventLookup.lookup(selectedDate.value.getYear(), selectedDate.value.getMonthOfYear())
    var debugString = ""
    var debugString2: String
    // get only the events for the selected date from the map
    var eventArray: Array<Event> = arrayOf()
    eventDayMap.forEach {
        if (it.first.any { it == selectedDate.value.getDayOfMonth() }) {eventArray += it.second}
        debugString2 = ""
        it.first.forEach { debugString2 += "$it " }
        debugString += "$debugString2 | ${it.second}\n"
    }
    Log.d("Calendar - eventDayMap", debugString)



    Column(modifier = modifier) {
        Text(redraw.value.toString())

        Column(modifier = Modifier.height(400.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Month changer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                // Left
                Box {
                    IconButton(onClick = { displayedDate.value.changeDate(TimeUnit.YEAR, -1); redraw.value += 1 }, modifier = Modifier.offset((-20).dp)){ Icon(
                        painterResource(R.drawable.chevron_double_left), contentDescription = "Go back 1 year") }
                    IconButton(onClick = { displayedDate.value.changeDate(TimeUnit.MONTH, -1); redraw.value += 1 }){ Icon(painterResource(R.drawable.chevron_left), contentDescription = "Go back 1 month") }
                }
                // Title
                Row(modifier = Modifier.width(200.dp), horizontalArrangement = Arrangement.Center) {Text(text = displayedDate.value.formatAsString(DateFormat.MONTHYEARLONG), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)}
                // Right
                Box {
                    IconButton(onClick = { displayedDate.value.changeDate(TimeUnit.MONTH, 1); redraw.value += 1 }){ Icon(painterResource(R.drawable.chevron_right), contentDescription = "Go forward 1 month") }
                    IconButton(onClick = { displayedDate.value.changeDate(TimeUnit.YEAR, 1); redraw.value += 1 }, modifier = Modifier.offset(20.dp)){ Icon(
                        painterResource(R.drawable.chevron_double_right), contentDescription = "Go forward 1 year") }
                }
            }
            // Month grid - display starting on first of month
            MonthDays(startOn = displayedDate.value.getDayOfWeekOfFirstOfMonth(), numDays = displayedDate.value.getNumDaysOfMonth(),
                selectedDay = (if (displayedDate.value.getYear() == selectedDate.value.getYear() && displayedDate.value.getMonthOfYear() == selectedDate.value.getMonthOfYear()) selectedDate.value.getDayOfMonth() else 0),
                onDaySelect = {day: Int ->
                    selectedDate.value.setDayOfMonth(day)
                    selectedDate.value.setMonthOfYear(displayedDate.value.getMonthOfYear())
                    selectedDate.value.setYear(displayedDate.value.getYear())
                    redraw.value += 1 }) // only show selected day if on selected year/month

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
                .fillMaxHeight(), selectedDate.value, eventArray)
            // Add event button
            val iconSize = 96
            Box(modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.BottomEnd)) {
                IconButton(onClick = { val intent = Intent(context, NewEvent::class.java).apply { putExtra("selectedDate", selectedDate.value.formatAsString(DateFormat.YYYYMMDD)) } // Start New Event activity
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
        else -> null
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Color spacer  TODO: update height based on # of lines of description
        Box(modifier = Modifier.width(6.dp).height((44).dp).clip(RoundedCornerShape(30.dp)).
                                background(c?: colorResource(R.color.beige)).
                                border(width = (0.4).dp, color = colorResource(R.color.blackshadow), shape = RoundedCornerShape(12.dp)))
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Column {
            Row {
                // Title & location
                Column(modifier = Modifier.width(260.dp)) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Row {Icon(painterResource(R.drawable.map_marker), contentDescription = "Location icon")
                        Text(location); Spacer(modifier = Modifier.padding(horizontal = 8.dp))}
                }
                Spacer(Modifier.weight(1f).fillMaxHeight())
                // Start and end time
                Column {
                    Text(start)
                    Text(end)
                }
            }
            // Description
            if (description.isNotEmpty()) {
                Text(description, color = colorResource(R.color.gray_css3), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column {
                Spacer(modifier = Modifier.padding(vertical = 2.dp))
                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().padding(end = 22.dp).background(colorResource(R.color.pinkbeige)))
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun Events(modifier: Modifier = Modifier, date: Date, eventArray: Array<Event>) {
    // Title
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Events for ${date.formatAsString(DateFormat.FULLLONG)}:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(10.dp))

        if (eventArray.isEmpty()) {
            Text("No events for this day")
        } else {
          LazyColumn{
              items(eventArray.size) { event ->
                  val startDate = eventArray[event].startDate
                  val endDate = eventArray[event].finalDate
                  var startTime = eventArray[event].startTime
                  var endTime = eventArray[event].endTime
                  if (startDate != date) {    // this event does not start on selected day
                      startTime += if (startDate.getYear() != date.getYear()) { " (${startDate.formatAsString(DateFormat.MONTHYEARSHORT)})" }  // event not in same year as start
                      else { " (${startDate.formatAsString(DateFormat.DAYMONTHSHORT)})" }  // event in same year but different month/day
                  }
                  if (endDate != date) {    // this event does not end on selected day
                      endTime += if (endDate == null) {" (No End)"}  // there is no end
                      else if (endDate.getYear() != date.getYear()) { " (${endDate.formatAsString(DateFormat.MONTHYEARSHORT)})" }  // event not in same year as end
                      else { " (${endDate.formatAsString(DateFormat.DAYMONTHSHORT)})" }  // event in same year but different month/day
                  }

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


