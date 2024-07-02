package com.example.nofusscalendar

import DTUtils
import Date
import DateFormat
import Event
import EventLookup
import ICSUtils
import TimeUnit
import VEvent
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme

class Calendar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = intent.getStringExtra("uri") ?: "No uri"  // URI of ICS file
        val icsRaw = ICSUtils.readTextFromUri(this@Calendar, uri)?: "~"  // raw ICS string
        val vevents = if (icsRaw == "~") { throw Exception("Error reading ICS file") } else ICSUtils.parseICS(icsRaw)  // VEvents array

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
                        , veventArray = vevents, uri = uri)
                }
            }
        }
    }
}

@Composable
fun Calendar(modifier: Modifier = Modifier, veventArray: Array<VEvent>, uri: String) {
    val context = LocalContext.current

    // States
    val displayEventDialog = remember{mutableStateOf(false)}
    val initialEvent = remember{mutableStateOf(Event("", "", "", "", "", 11, 0, 12, 0, Date(1990, 1, 1), Date(1990, 1, 1), null))}

    // Event Lookup class from VEvent array
    val vevents = remember { mutableStateOf(veventArray) }
    val eventLookup = EventLookup(); eventLookup.createFromVevents(vevents.value)

    // Display NewEvent dialog / MainScreen
    if (displayEventDialog.value) {
        EventDialog(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(colorResource(R.color.beige)),
            initialEvent.value,
            onCancel = {displayEventDialog.value = false},
            onConfirm = { vevent: VEvent ->
                var createNewVevent = true
                for (veventI in vevents.value.indices) {
                    if (vevents.value[veventI].getUID() == vevent.getUID()) {  // same vevent, should override instead of creating a new one
                        vevents.value[veventI] = vevent
                        createNewVevent = false
                        break
                    }
                }
                if (createNewVevent) {vevents.value += vevent}  // no vevent with same UID found, create a new one
                ICSUtils.writeVEventsToUri(context, uri, vevents.value)
                displayEventDialog.value = false},
            onDelete = {uid: String ->
                for (veventI in vevents.value.indices) {
                    if (vevents.value[veventI].getUID() == uid) {  // delete event with matching UID
                        vevents.value[veventI] = VEvent(arrayOf(), null)
                        break
                    }
                }
                ICSUtils.writeVEventsToUri(context, uri, vevents.value)
                displayEventDialog.value = false
            })

    } else {
        MainScreen(modifier = modifier, eventLookup = eventLookup,
            onNewEvent = { initialDate: Date -> displayEventDialog.value = true; initialEvent.value = Event("", "", "", "", "", 11, 0, 12, 0, initialDate, initialDate, null) },
            onEditEvent = { event: Event -> initialEvent.value = event; displayEventDialog.value = true })
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, eventLookup: EventLookup, onNewEvent: (initialDate: Date) -> Unit, onEditEvent: (initialEvent: Event) -> Unit) {
    val redraw = remember { mutableStateOf(0) }

    // Displayed date
    val calDate = remember { mutableStateOf(DTUtils.getNow()) }

    // eventsDayMap for this month from lookup
    val eventDayMap = eventLookup.lookup(calDate.value.getYear(), calDate.value.getMonthOfYear())
    val hasEventArray = BooleanArray(31)
    // get only the events for the selected date from the map
    var eventArray: Array<Event> = arrayOf()
    eventDayMap.forEach {
        var check = true
        for (day in it.first) {
            if (check && day == calDate.value.getDayOfMonth()) {eventArray += it.second; check = false}
            hasEventArray[day-1] = true
        }
    }

    Column(modifier = modifier) {
        Text(redraw.value.toString(), fontSize = 0.sp, lineHeight = 0.sp)

        Column(modifier = Modifier.height(400.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Month changer
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                // Left
                Box {
                    IconButton(onClick = { calDate.value.changeDate(TimeUnit.YEAR, -1); redraw.value += 1 }, modifier = Modifier.offset((-20).dp)){ Icon(
                        painterResource(R.drawable.chevron_double_left), contentDescription = "Go back 1 year") }
                    IconButton(onClick = { calDate.value.changeDate(TimeUnit.MONTH, -1); redraw.value += 1 }){ Icon(painterResource(R.drawable.chevron_left), contentDescription = "Go back 1 month") }
                }
                // Title
                Row(modifier = Modifier.width(200.dp), horizontalArrangement = Arrangement.Center) {Text(text = calDate.value.formatAsString(DateFormat.MONTHYEARLONG), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)}
                // Right
                Box {
                    IconButton(onClick = { calDate.value.changeDate(TimeUnit.MONTH, 1); redraw.value += 1 }){ Icon(painterResource(R.drawable.chevron_right), contentDescription = "Go forward 1 month") }
                    IconButton(onClick = { calDate.value.changeDate(TimeUnit.YEAR, 1); redraw.value += 1 }, modifier = Modifier.offset(20.dp)){ Icon(
                        painterResource(R.drawable.chevron_double_right), contentDescription = "Go forward 1 year") }
                }
            }
            // Month grid - display starting on first of month
            MonthDays(startOn = calDate.value.getDayOfWeekOfFirstOfMonth(), numDays = calDate.value.getNumDaysOfMonth(),
                hasEventArray = hasEventArray,
                selectedDay = (if (calDate.value.getYear() == calDate.value.getYear() && calDate.value.getMonthOfYear() == calDate.value.getMonthOfYear()) calDate.value.getDayOfMonth() else 0),
                onDaySelect = {day: Int ->
                    calDate.value.setDayOfMonth(day)
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
                .fillMaxHeight(), calDate.value, eventArray, { event: Event -> onEditEvent(event) })
            // Add event button
            val iconSize = 96
            Box(modifier = Modifier
                .padding(vertical = 20.dp)
                .align(Alignment.BottomEnd)) {
                IconButton(onClick = { onNewEvent(calDate.value) }  // Start New Event activity
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
        "blue" -> colorResource(R.color.blue_css3)
        "teal" -> colorResource(R.color.teal_css3)
        "aqua" -> colorResource(R.color.aqua_css3)
        else -> null
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        // Color spacer  TODO: update height based on # of lines of description
        Box(modifier = Modifier
            .width(6.dp)
            .height((44).dp)
            .clip(RoundedCornerShape(30.dp))
            .background(c ?: colorResource(R.color.beige))
            .border(
                width = (0.4).dp,
                color = colorResource(R.color.blackshadow),
                shape = RoundedCornerShape(12.dp)
            ))
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Column {
            Row {
                // Title & location
                Column(modifier = Modifier.width(248.dp)) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Row {Icon(painterResource(R.drawable.map_marker), contentDescription = "Location icon")
                        Text(location); Spacer(modifier = Modifier.padding(horizontal = 8.dp))}
                }
                Spacer(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight())
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
                Spacer(modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .padding(end = 22.dp)
                    .background(colorResource(R.color.pinkbeige)))
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun Events(modifier: Modifier = Modifier, date: Date, eventArray: Array<Event>, onEditEvent: (initialEvent: Event) -> Unit) {
    // Title
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Events for ${date.formatAsString(DateFormat.FULLLONG)}:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(10.dp))

        if (eventArray.isEmpty()) {
            Row(modifier = Modifier.padding(horizontal = 24.dp)) {Text("No events for this day")}
        } else {
          LazyColumn{
              items(eventArray.size) { event ->
                  val te = eventArray[event]
                  val startDate = te.startDate
                  val endDate = te.finalDate
                  var startTime = if (te.startHour == -1) "all-day" else "${te.startHour.toString().padStart(2, '0')}:${te.startMinute.toString().padStart(2, '0')}"
                  var endTime = if (te.startHour == -1) "" else "${te.endHour.toString().padStart(2, '0')}:${te.endMinute.toString().padStart(2, '0')}"
                  if (startDate != date) {    // this event does not start on selected day
                      startTime += if (startDate.getYear() != date.getYear()) { " (${startDate.formatAsString(DateFormat.MONTHYEARSHORT)})" }  // event not in same year as start
                      else { " (${startDate.formatAsString(DateFormat.DAYMONTHSHORT)})" }  // event in same year but different month/day
                  }
                  if (endDate != date) {    // this event does not end on selected day
                      endTime += if (endDate == null) {" (No End)"}  // there is no end
                      else if (endDate.getYear() != date.getYear()) { " (${endDate.formatAsString(DateFormat.MONTHYEARSHORT)})" }  // event not in same year as end
                      else { " (${endDate.formatAsString(DateFormat.DAYMONTHSHORT)})" }  // event in same year but different month/day
                  }
                  Box {
                      TextButton(onClick = { onEditEvent(te) }, modifier = Modifier.fillMaxWidth().height(44.dp)) {}
                      EventItem(
                          title = te.title,
                          location = te.location,
                          description = te.description,
                          color = te.color,
                          start = startTime,
                          end = endTime
                      )
                  }
                  if (event == eventArray.size - 1) {Spacer(modifier = Modifier.height(100.dp))}
              }
          }
        }
    }
}


@Composable
fun DaySelect(modifier: Modifier = Modifier, day: Int, selected: Boolean, hasEvent: Boolean, onClick: () -> Unit) {
    // Highlight color when selected
    val c = when(selected){
        true -> colorResource(R.color.buttongreenhighlight)
        false -> colorResource(R.color.buttongreen)
    }
    val b = when(hasEvent) {
        true -> 2
        false -> 0
    }

    // Day box
    Box(modifier = modifier
        .fillMaxWidth()
        .background(c)
        .border(width = b.dp, color = colorResource(R.color.blackshadow))) {
        TextButton(onClick = onClick){}
        Text(text = day.toString(), Modifier.align(Alignment.Center), color = colorResource(R.color.whitehighlight))
    }
}


@Composable
fun MonthDays(startOn: Int, numDays: Int, hasEventArray: BooleanArray, selectedDay: Int, onDaySelect: (Int) -> Unit) {
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
                else -> DaySelect(day = day - startOn + 2, selected = selectedDay == (day - startOn + 2),
                    hasEvent = hasEventArray[day - startOn + 1], onClick = {onDaySelect(day - startOn + 2)})  // first day to last
            }
        }
    }
}


