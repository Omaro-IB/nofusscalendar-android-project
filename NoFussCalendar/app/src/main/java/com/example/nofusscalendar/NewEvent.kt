package com.example.nofusscalendar
import DTUtils
import Date
import DateFormat
import Event
import Frequency
import ICSUtils
import RRule
import TimeUnit
import UntilWhat
import VEvent
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


fun getColorResource(color: String): Int {
    return when(color){
        "black" -> R.color.black_css3
        "silver" -> R.color.silver_css3
        "gray" -> R.color.gray_css3
        "white" -> R.color.white_css3
        "maroon" -> R.color.maroon_css3
        "red" -> R.color.red_css3
        "purple" -> R.color.purple_css3
        "fuchsia" -> R.color.fuchsia_css3
        "green" -> R.color.green_css3
        "lime" -> R.color.lime_css3
        "olive" -> R.color.olive_css3
        "yellow" -> R.color.yellow_css3
        "navy" -> R.color.navy_css3
        "blue" -> R.color.blue_css3
        "teal" -> R.color.teal_css3
        "aqua" -> R.color.aqua_css3
        else -> R.color.textfield
    }
}

val colors = arrayOf("black", "silver", "gray", "white", "maroon", "red", "purple", "fuchsia", "green", "lime", "olive", "yellow", "navy", "blue", "teal", "aqua")


@Composable
fun ColorPickerDialog(onColorSelected: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4)
            ) {
                items(16) { color ->
                    Box(modifier = Modifier
                        .height(50.dp)
                        .width(30.dp)
                        .background(colorResource(getColorResource(colors[color])))) {
                        TextButton(onClick = { onColorSelected(colors[color]) }) {  }
                    }
                }
            }
            TextButton(onClick = {onColorSelected("")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("No Color")}
        }
    }
}

@Composable
fun FrequencyPickerDialog(onSelected: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            TextButton(onClick = {onSelected("days")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("days")}
            TextButton(onClick = {onSelected("weeks")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("weeks")}
            TextButton(onClick = {onSelected("months")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("months")}
            TextButton(onClick = {onSelected("years")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("years")}
        }
    }
}

@Composable
fun UntilPickerDialog(onSelected: (String) -> Unit, onDismiss: () -> Unit) {
    // onSelected -> "forever" or "YYYYMMDDT000000" or "<Digit>" (digit if number of occurrences)
    val mContext = LocalContext.current
    val yearFromNow = DTUtils.getNow()
    yearFromNow.changeDate(TimeUnit.YEAR, 1)
//    val untilYear = remember { mutableStateOf(yearFromNow.getYear()) }
//    val untilMonth = remember { mutableStateOf(yearFromNow.getMonthOfYear()) }
//    val untilDay = remember { mutableStateOf(yearFromNow.getDayOfMonth()) }
    val untilCount = remember { mutableStateOf("10") }

    val mDatePickerDialogUntil = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            onSelected("d${mYear}${(mMonth+1).toString().padStart(2, '0')}${mDayOfMonth.toString().padStart(2, '0')}")
        }, yearFromNow.getYear(), yearFromNow.getMonthOfYear()-1, yearFromNow.getDayOfMonth()
    )


    Dialog(onDismissRequest = onDismiss){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            TextButton(onClick = {onSelected("forever")}, modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("Forever (no end)")}
            TextButton(onClick = {mDatePickerDialogUntil.show();},
                modifier = Modifier.align(Alignment.CenterHorizontally)) {Text("Select a date")}
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                TextButton(onClick = {onSelected(untilCount.value)}) {Text("After this number of times:")}
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .offset(y = (-20).dp)
                ) {
                    TextField(
                        value = untilCount.value,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            if (it.length < 4) {
                                untilCount.value = it
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RRuleDialog(onRRuleSelected: (RRule?) -> Unit, onDismiss: () -> Unit) {
    val selectedInterval = remember { mutableStateOf("1") }
    var showFreqDialog by remember { mutableStateOf(false) }
    var selectedFreq by remember { mutableStateOf("days") }
    var showUntilDialog by remember { mutableStateOf(false) }
    var selectedUntil by remember { mutableStateOf("forever") }
    Log.d("RRuleDialog", "$selectedInterval.value $selectedFreq")

    Dialog(onDismissRequest = onDismiss){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .width(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(30.dp)) {
                Row {
                    Text("Repeat every ")
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .offset(y = (-20).dp)
                    ) {
                        TextField(
                            value = selectedInterval.value,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = {
                                if (it.length < 3) {
                                    selectedInterval.value = it
                                }
                            }
                        )
                    }
                    TextButton(
                        onClick = { showFreqDialog = true },
                        modifier = Modifier.width(110.dp).offset(y = (-10).dp, x = (-5).dp)
                    ) { Text(selectedFreq) }
                    if (showFreqDialog) {
                        FrequencyPickerDialog(onSelected = { freq: String ->
                            selectedFreq = freq; showFreqDialog = false
                        }) {}
                    }
                }
                Row {
                    Text("Until ")
                    TextButton(
                        onClick = { showUntilDialog = true },
                        modifier = Modifier.width(150.dp).offset(y = (-15).dp, x = (-5).dp)
                    ) { Text(
                        if (selectedUntil[0] == 'd') DTUtils.parseDateStringToDate(selectedUntil.slice(1..8)).formatAsString(DateFormat.DAYMONTHYEARSHORT)
                        else if (selectedUntil[0] == 'f') "forever"
                        else "after $selectedUntil times"
                    ) }
                    if (showUntilDialog) {
                        UntilPickerDialog(onSelected = { until: String ->
                            selectedUntil = until; showUntilDialog = false
                        }) {}
                    }
                }
//            TextButton(onClick = { onRRuleSelected(RRule(Frequency.YEARLY, 1, null, null, UntilWhat.OCCURRENCES, "4")) }) { Text("Repeat every year 4 times") }
                Row {
                    Button(onClick = { onRRuleSelected(null) }, modifier = Modifier.padding(end = 5.dp)) { Text("No Repeat") }
                    Button(onClick = {
                        val frequency = when (selectedFreq) {
                            "days" -> Frequency.DAILY
                            "weeks" -> Frequency.WEEKLY
                            "months" -> Frequency.MONTHLY
                            "years" -> Frequency.YEARLY
                            else -> throw Exception("Something went wrong with the frequency picker")
                        }
                        val untilWhat: UntilWhat?
                        val untilVal: String?
                        if (selectedUntil == "forever") {
                            untilWhat = null; untilVal = null
                        } else if (selectedUntil.matches(Regex("\\d+"))) {
                            untilWhat = UntilWhat.OCCURRENCES; untilVal = selectedUntil
                        } else if (selectedUntil[0] == 'd') {
                            untilWhat = UntilWhat.DATE; untilVal = selectedUntil.slice(1..8)
                        } else {
                            throw Exception("Something went wrong with the repeat-until picker")
                        }
                        onRRuleSelected(
                            RRule(
                                frequency,
                                selectedInterval.value.toInt(),
                                null,
                                null,
                                untilWhat,
                                untilVal
                            )
                        )
                    }) { Text("Done") }
                }
            }
        }
    }
}


@Composable
fun EventDialog(modifier: Modifier = Modifier, initialEvent: Event, onCancel: () -> Unit, onConfirm: (vevent: VEvent) -> Unit, onDelete: (uid: String) -> Unit) {
    // States
    val redraw = remember { mutableStateOf(0) }
    // End Date/Time
    val endYear = remember { mutableStateOf(initialEvent.endDate.getYear()) }
    val endMonth = remember { mutableStateOf(initialEvent.endDate.getMonthOfYear()) }
    val endDay = remember { mutableStateOf(initialEvent.endDate.getDayOfMonth()) }
    val endHour = remember { mutableStateOf(if (initialEvent.endHour == -1) {12} else {initialEvent.endHour}) }
    val endMinute = remember { mutableStateOf(if (initialEvent.endMinute == -1) {0} else {initialEvent.endMinute}) }
    // Start Time
    val startHour = remember { mutableStateOf(if (initialEvent.startHour == -1) {11} else {initialEvent.startHour}) }
    val startMinute = remember { mutableStateOf(if (initialEvent.startMinute == -1) {0} else {initialEvent.startMinute}) }
    // Event info
    var title by remember { mutableStateOf(initialEvent.title) }
    var description by remember { mutableStateOf(initialEvent.description) }
    var location by remember { mutableStateOf(initialEvent.location) }
    var allDay by remember { mutableStateOf(initialEvent.endHour == -1 || initialEvent.endMinute == -1 || initialEvent.startHour == -1 || initialEvent.startMinute == -1) }
    // Color and RRule
    var showColorDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(initialEvent.color) }
    var showRRuleDialog by remember { mutableStateOf(false) }
    var selectedRRule: RRule? by remember { mutableStateOf(null) }
    val c = colorResource(getColorResource(selectedColor))

    // Fetching the Local Context
    val mContext = LocalContext.current

    // Add text field modifier for consistency
    val textFieldModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 40.dp)
        .height(50.dp)

    Column (modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(redraw.value.toString(), lineHeight = 0.sp, fontSize = 0.sp)
        Spacer(modifier = Modifier.height(20.dp))
        // Title & Cancel/Add buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onCancel() } ) {  // exit to MainActivity
                Text("Cancel", color = colorResource(R.color.buttonred))
            }
            Text("Event", fontWeight = FontWeight.Bold)
            TextButton(onClick = {
                val vevent: VEvent
                val uid = if (initialEvent.uid == "") {null} else {initialEvent.uid}
                if (allDay) {
                    val endDate_ = Date(initialEvent.startDate.getYear(), initialEvent.startDate.getMonthOfYear(), initialEvent.startDate.getDayOfMonth()); endDate_.changeDate(TimeUnit.DAY, 1)
                    vevent = ICSUtils.createVEvent(uid, title, location, description, selectedColor, initialEvent.startDate, endDate_, null, null, null, null, selectedRRule)
                } else {
                    vevent = ICSUtils.createVEvent(uid, title, location, description, selectedColor, initialEvent.startDate, Date(endYear.value, endMonth.value, endDay.value), startHour.value, startMinute.value, endHour.value, endMinute.value, selectedRRule)
                }
                onConfirm(vevent)}) {
                Text("Confirm", color = colorResource(R.color.buttongreen))
            }
        }

        // Title, Description, Location fields
        TextField(value = title, onValueChange = { title = it }, placeholder = { Text("Title") }, modifier = textFieldModifier)
        TextField(value = location, onValueChange = { location = it }, placeholder = { Text("Location or Video Call") }, modifier = textFieldModifier)
        TextField(value = description, onValueChange = { description = it }, placeholder = { Text("Description") }, modifier = textFieldModifier)

        Spacer(modifier = Modifier.height(15.dp))
        // Color Dialog
        Box {
            TextField(value = "Select Color", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            OutlinedButton(onClick = { showColorDialog = true }, modifier = Modifier
                .offset(250.dp)
                .width(110.dp), colors = ButtonDefaults.buttonColors(containerColor = c)) {Text(if (selectedColor == "") "SELECT" else selectedColor.uppercase())}
        }
        if (showColorDialog) {
            ColorPickerDialog(onColorSelected = {color: String -> selectedColor = color; showColorDialog = false}) {}
        }

        // RRule Dialog
        Box {
            TextField(value = "Set Event Repeat", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            OutlinedButton(onClick = { showRRuleDialog = true }, modifier = Modifier
                .offset(250.dp)
                .width(110.dp)) {Text("SET")}
        }
        if (showRRuleDialog) {
            RRuleDialog(onRRuleSelected = {rrule: RRule? -> selectedRRule = rrule; showRRuleDialog = false}) {}
        }

        Text(if (selectedRRule == null) "This event does not repeat" else "This event repeats $selectedRRule",
            modifier = Modifier.padding(vertical = 3.dp), textAlign = TextAlign.Center, fontSize = 15.sp, color = colorResource(R.color.gray_css3))


        Spacer(modifier = Modifier.height(15.dp))
        // All-day toggle
        Box {
            TextField(value = "All-day", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Switch(checked = allDay, onCheckedChange = { allDay = it }, modifier = Modifier.offset(305.dp))
        }

        val dateTimeAlpha = if (allDay) 0f else 1f

        // Declaring DatePickerDialog and setting -- end date
        val mDatePickerDialogEnd = DatePickerDialog(
            mContext,
            { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                redraw.value += 1
                endYear.value = mYear
                endMonth.value = mMonth+1
                endDay.value = mDayOfMonth
            }, initialEvent.startDate.getYear(), initialEvent.startDate.getMonthOfYear()-1, initialEvent.startDate.getDayOfMonth()
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

        // Start date and time
        Box(modifier = Modifier.alpha(dateTimeAlpha)) {
            TextField(value = "Starts", onValueChange = {}, enabled = false, singleLine = true, modifier = textFieldModifier)
            Row(modifier = Modifier.offset(125.dp)){
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), border = BorderStroke(1.dp, colorResource(R.color.textfielddisabled))){
                    Text(initialEvent.startDate.formatAsString(DateFormat.DAYMONTHYEARSHORT), color = colorResource(R.color.textfielddisabled))
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
                    Text("${DTUtils.monthIntToStr(endMonth.value, short = true)} ${endDay.value} ${endYear.value}", color = colorResource(R.color.buttongreengradient))
                }
                OutlinedButton(onClick = { mTimePickerDialogEnd.show() }, shape = RoundedCornerShape(5), colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.textfield)), modifier = Modifier.width(110.dp)){
                    Text(DTUtils.timeToStr(endHour.value, endMinute.value), color = colorResource(R.color.buttongreengradient))
                }
            }
        }

        Button(onClick = {onDelete(initialEvent.uid)}) {Text("Delete")}
    }
}