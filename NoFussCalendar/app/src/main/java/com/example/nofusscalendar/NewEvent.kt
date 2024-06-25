package com.example.nofusscalendar
import DTUtils
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
        setContent {
            NoFussCalendarTheme {
                Column {
                    Spacer(modifier= Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                        .background(colorResource(R.color.beigedark)))
                    EventDialog(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(colorResource(R.color.beige)),
                        2024, 6, 25)  // TODO: Update day based on selected day
                }
            }
        }
    }
}

@Composable
fun EventDialog(modifier: Modifier = Modifier, startYear: Int, startMonth: Int, startDay: Int) {
    // Fetching the Local Context
    val mContext = LocalContext.current

    // States
    // End Date
    val endYear = remember { mutableStateOf(startYear) }
    val endMonth = remember { mutableStateOf(startMonth) }
    val endDay = remember { mutableStateOf(startDay) }
    val endHour = remember { mutableStateOf(12) }
    val endMinute = remember { mutableStateOf(0) }
    // Start Date
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
            endYear.value = mYear
            endMonth.value = mMonth + 1
            endDay.value = mDayOfMonth
        }, startYear, startMonth-1, startDay
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { /* TODO: exit activity */ }
            ) {
                Text("Cancel", color = colorResource(R.color.buttonred))
            }
            Text("New Event", fontWeight = FontWeight.Bold)
            TextButton(
                onClick = { /* TODO: exit activity */ }
            ) {
                Text("Add", color = colorResource(R.color.buttongreen))
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        TextField(value = title, onValueChange = { title = it }, placeholder = { Text("Title") }, modifier = textFieldModifier)
        TextField(value = location, onValueChange = { location = it }, placeholder = { Text("Location or Video Call") }, modifier = textFieldModifier)
        Spacer(modifier = Modifier.height(15.dp))
        Box {
            TextField(value = "All-day", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Switch(checked = allDay, onCheckedChange = { allDay = it }, modifier = Modifier.offset(305.dp))
        }
        Box(modifier = Modifier.alpha(dateTimeAlpha)) {
            TextField(value = "Starts", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Row(modifier = Modifier.offset(125.dp)){
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), border = BorderStroke(1.dp, colorResource(R.color.textfielddisabled))){
                    Text("${DTUtils.monthIntToStr(startMonth, short = true)} $startDay $startYear", color = colorResource(R.color.textfielddisabled))
                }
                OutlinedButton(onClick = { mTimePickerDialogStart.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), modifier = Modifier.width(110.dp)){
                    Text(DTUtils.timeToStr(startHour.value, startMinute.value), color = colorResource(R.color.buttongreengradient))
                }
            }
        }
        Box(modifier = Modifier.alpha(dateTimeAlpha)) {
            TextField(value = "Ends", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Row(modifier = Modifier.offset(125.dp)){
                OutlinedButton(onClick = { mDatePickerDialogEnd.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield))){
                    Text("${DTUtils.monthIntToStr(endMonth.value, short = true)} ${endDay.value} ${endYear.value}", color = colorResource(R.color.buttongreengradient))
                }
                OutlinedButton(onClick = { mTimePickerDialogEnd.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), modifier = Modifier.width(110.dp)){
                    Text(DTUtils.timeToStr(endHour.value, endMinute.value), color = colorResource(R.color.buttongreengradient))
                }
            }
        }
    }
}