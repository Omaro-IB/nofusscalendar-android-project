package com.example.nofusscalendar
import Event
import DTUtils
import Date
import DateFormat
import ICSUtils
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nofusscalendar.ui.theme.NoFussCalendarTheme

class NewEvent : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val selectedDate = intent.getStringExtra("selectedDate") ?: "19900101"
        val uri = intent.getStringExtra("uri") ?: throw Exception("Something went terribly wrong; no URI passed into NewEvent")
        setContent {
            NoFussCalendarTheme {
                Column {
                    // Dark space
                    Spacer(modifier= Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .background(colorResource(R.color.beigedark)))
                    EventDialog(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(colorResource(R.color.beige)), DTUtils.parseDateStringToDate(selectedDate), uri)
                }
            }
        }
    }
}

@Composable
fun EventDialog(modifier: Modifier = Modifier, date: Date, uri: String) {
    // Fetching the Local Context
    val mContext = LocalContext.current

    // States  TODO: set initial value from intent
    // End Date/Time
    val endDate: Date by remember { mutableStateOf(date) }
    val endHour = remember { mutableStateOf(12) }
    val endMinute = remember { mutableStateOf(0) }
    // Start Date / Time
    val startDate: Date by remember { mutableStateOf(date) }
    val startHour = remember { mutableStateOf(11) }
    val startMinute = remember { mutableStateOf(0) }
    // Event info
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }

    val dateTimeAlpha = if (allDay) 0f else 1f

    // Declaring DatePickerDialog and setting -- end date
    val mDatePickerDialogEnd = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            endDate.setYear(mYear)
            endDate.setMonthOfYear(mMonth + 1)
            endDate.setDayOfMonth(mDayOfMonth)
        }, startDate.getYear(), startDate.getMonthOfYear()-1, startDate.getDayOfMonth()
    )

    // Declaring DatePickerDialog and setting -- end time
    val mTimePickerDialogEnd = TimePickerDialog(
        mContext,
        { _: TimePicker, mHour: Int, mMinute: Int ->
            endHour.value = mHour
            endMinute.value = mMinute
        }, endHour.value, endMinute.value, true
    )

    // Declaring DatePickerDialog and setting -- start time
    val mTimePickerDialogStart = TimePickerDialog(
        mContext,
        { _: TimePicker, mHour: Int, mMinute: Int ->
            startHour.value = mHour
            startMinute.value = mMinute
        }, startHour.value, startMinute.value, true
    )

    // Add text fields for consistency
    val textFieldModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 40.dp)
        .height(50.dp)

    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        // Title & Cancel/Add buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { mContext.startActivity(Intent(mContext, MainActivity::class.java)) }  // exit to MainActivity
            ) {
                Text("Cancel", color = colorResource(R.color.buttonred))
            }
            Text("Event", fontWeight = FontWeight.Bold)
            TextButton(
                onClick = { val event = Event()  // TODO: use intent to get all this event info (also needed when editing an event)
                            ICSUtils.addEventToICS(uri, event); mContext.startActivity(Intent(mContext, MainActivity::class.java)) }
            ) {
                Text("Confirm", color = colorResource(R.color.buttongreen))
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        // Title and Location fields
        TextField(value = title, onValueChange = { title = it }, placeholder = { Text("Title") }, modifier = textFieldModifier)
        TextField(value = location, onValueChange = { location = it }, placeholder = { Text("Location or Video Call") }, modifier = textFieldModifier)
        Spacer(modifier = Modifier.height(15.dp))

        // All-day toggle
        Box {
            TextField(value = "All-day", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Switch(checked = allDay, onCheckedChange = { allDay = it }, modifier = Modifier.offset(305.dp))
        }

        // Start date and time
        Box(modifier = Modifier.alpha(dateTimeAlpha)) {
            TextField(value = "Starts", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Row(modifier = Modifier.offset(125.dp)){
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), border = BorderStroke(1.dp, colorResource(R.color.textfielddisabled))){
                    Text(startDate.formatAsString(DateFormat.DAYMONTHYEARSHORT), color = colorResource(R.color.textfielddisabled))
                }
                OutlinedButton(onClick = { mTimePickerDialogStart.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), modifier = Modifier.width(110.dp)){
                    Text(DTUtils.timeToStr(startHour.value, startMinute.value), color = colorResource(R.color.buttongreengradient))
                }
            }
        }

        // End date and time
        Box(modifier = Modifier.alpha(dateTimeAlpha)) {
            TextField(value = "Ends", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Row(modifier = Modifier.offset(125.dp)){
                OutlinedButton(onClick = { mDatePickerDialogEnd.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield))){
                    Text(endDate.formatAsString(DateFormat.DAYMONTHYEARSHORT), color = colorResource(R.color.buttongreengradient))
                }
                OutlinedButton(onClick = { mTimePickerDialogEnd.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), modifier = Modifier.width(110.dp)){
                    Text(DTUtils.timeToStr(endHour.value, endMinute.value), color = colorResource(R.color.buttongreengradient))
                }
            }
        }
    }
}