package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import com.tadiwanashe.tutictlabs.Models.LabShift
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementView(
    viewModel: LabViewModel = viewModel(),
    navController: NavController
) {
    val tutors by viewModel.tutors.collectAsState()
    val labShifts by viewModel.labShifts.collectAsState()
    val context = LocalContext.current

    var selectedTutorId by remember { mutableStateOf<String?>(null) }
    var selectedDay by remember { mutableStateOf("Monday") }
    var selectedTime by remember { mutableStateOf("16:00 - 19:00") }
    var selectedLab by remember { mutableStateOf("Lab 10 - 138") }
    var showError by remember { mutableStateOf(false) }
    var showAddSuccess by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var shiftToDelete by remember { mutableStateOf<LabShift?>(null) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val times = listOf("16:00 - 19:00", "19:00 - 22:00", "18:00 - 20:00", "20:00 - 22:00")
    val labs = listOf("Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Schedule Management") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyColumn {
                item {
                    Card(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Select Tutor", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            var expanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedTutorId?.let { id ->
                                        tutors.firstOrNull { it.id == id }?.name ?: "Select a tutor"
                                    } ?: "Select a tutor")
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    tutors.forEach { tutor ->
                                        DropdownMenuItem(
                                            text = { Text(tutor.name) }, // Correct DropdownMenuItem syntax
                                            onClick = {
                                                selectedTutorId = tutor.id
                                                expanded = false
                                            })

                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Select Shift Details", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Day Picker
                            Text("Day", style = MaterialTheme.typography.labelMedium)
                            var dayExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { dayExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedDay)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = dayExpanded,
                                    onDismissRequest = { dayExpanded = false }
                                ) {
                                    days.forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text(day) }, // Correct DropdownMenuItem syntax
                                            onClick = {
                                                selectedDay = day
                                                var expanded = false
                                            })

                                    }
                                }
                            }

                            // Time Picker
                            Text("Time", style = MaterialTheme.typography.labelMedium)
                            var timeExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { timeExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedTime)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = timeExpanded,
                                    onDismissRequest = { timeExpanded = false }
                                ) {
                                    times.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time) }, // Correct DropdownMenuItem syntax
                                            onClick = {
                                                selectedTime = (time)
                                                var expanded = false
                                            })

                                    }
                                }
                            }

                            // Lab Picker
                            Text("Lab", style = MaterialTheme.typography.labelMedium)
                            var labExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { labExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedLab)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = labExpanded,
                                    onDismissRequest = { labExpanded = false }
                                ) {
                                    labs.forEach { lab ->
                                        DropdownMenuItem(
                                            text = { Text(lab) }, // Correct DropdownMenuItem syntax
                                            onClick = {
                                                selectedLab = (lab)
                                                var expanded = false
                                            })
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    selectedTutorId?.let { tutorId ->
                                        if (!viewModel.assignShift(
                                                tutorId = tutorId,
                                                day = selectedDay,
                                                time = selectedTime,
                                                labName = selectedLab
                                            )) {
                                            showError = true
                                        } else {
                                            showAddSuccess = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedTutorId != null
                            ) {
                                Text("Assign Shift")
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Assigned Shifts", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (labShifts.isEmpty()) {
                                Text(
                                    "No shifts assigned",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            } else {
                                Column {
                                    labShifts.forEach { shift ->
                                        val tutor = tutors.firstOrNull { it.id == shift.tutorId }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tutor?.name ?: "Unknown",
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    text = "${shift.day}, ${shift.time} at ${shift.labName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    shiftToDelete = shift
                                                    showDeleteConfirm = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            val today = calendar.time

                            calendar.firstDayOfWeek = Calendar.MONDAY
                            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                            val startOfWeek = calendar.time

                            calendar.add(Calendar.DAY_OF_YEAR, 6)
                            val endOfWeek = calendar.time

                            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                            val weekRange = "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"

                            viewModel.fetchWeeklyLabSessions(startDate = startOfWeek, endDate = endOfWeek) { sessions ->

                                val pdfUri = viewModel.generateWeeklyLabReport(
                                    sessions = sessions,
                                    weekRange = weekRange,
                                    context = context
                                )

                                pdfUri?.let { uri ->
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share Weekly Lab Report")
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Download Weekly Lab Report")
                    }
                }
            }
        }
    }

    // Alerts
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Shift Already Exists") },
            text = { Text("This shift has already been assigned. Please select a different day, time, or lab.") },
            confirmButton = {
                Button(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showAddSuccess) {
        AlertDialog(
            onDismissRequest = { showAddSuccess = false },
            title = { Text("Shift Assigned") },
            text = { Text("The shift has been successfully assigned.") },
            confirmButton = {
                Button(onClick = { showAddSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Shift?") },
            text = { Text("Are you sure you want to delete this shift?") },
            confirmButton = {
                Button(
                    onClick = {
                        shiftToDelete?.let { viewModel.deleteShift(it) }
                        shiftToDelete = null
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = {
                    shiftToDelete = null
                    showDeleteConfirm = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}