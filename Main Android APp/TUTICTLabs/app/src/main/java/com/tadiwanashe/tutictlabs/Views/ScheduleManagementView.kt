package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import com.tadiwanashe.tutictlabs.Models.LabShift
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementView(
    viewModel: LabViewModel
) {
    var selectedTutorId by remember { mutableStateOf<String?>(null) }
    var selectedDay by remember { mutableStateOf("Monday") }
    var selectedTime by remember { mutableStateOf("09:00 - 11:00") }
    var selectedLab by remember { mutableStateOf("Lab 10 - 138") }
    var showError by remember { mutableStateOf(false) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val times = listOf("09:00 - 11:00", "11:00 - 13:00", "14:00 - 16:00")
    val labs = listOf("Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06")

    LaunchedEffect(Unit) {
        viewModel.fetchTutors()
        viewModel.fetchShifts()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // Tutor Selection
        Text("Select Tutor", style = MaterialTheme.typography.labelMedium)
        LabeledDropdown(
                label = "Select Tutor",
        options = viewModel.tutors.value.map { it.name },
        selectedOption = viewModel.tutors.value.find { it.id == selectedTutorId }?.name ?: "Select Tutor",
        onOptionSelected = { selectedName ->
            selectedTutorId = viewModel.tutors.value.find { it.name == selectedName }?.id
        }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shift Details
        Text("Select Shift Details", style = MaterialTheme.typography.labelMedium)
        LabeledDropdown(
            label = "Select Day",
            options = days,
            selectedOption = selectedDay,
            onOptionSelected = { selectedDay = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LabeledDropdown(
            label = "Select Time",
            options = times,
            selectedOption = selectedTime,
            onOptionSelected = { selectedTime = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        LabeledDropdown(
            label = "Select Lab",
            options = labs,
            selectedOption = selectedLab,
            onOptionSelected = { selectedLab = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedTutorId?.let { tutorId ->
                    if (!viewModel.assignShift(tutorId, selectedDay, selectedTime, selectedLab)) {
                        showError = true
                    }
                }
            },
            enabled = selectedTutorId != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Assign Shift")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Assigned Shifts
        Text("Assigned Shifts", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(viewModel.labShifts.value) { shift ->
                val tutor = viewModel.tutors.value.find { it.id == shift.tutorId }
                ShiftItem(
                    shift = shift,
                    tutorName = tutor?.name ?: "Unknown",
                    onDelete = { viewModel.deleteShift(shift) }
                )
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text("This lab is already assigned for the selected day and time.") },
            confirmButton = {
                Button(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ShiftItem(shift: LabShift, tutorName: String, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tutorName, style = MaterialTheme.typography.titleSmall)
                Text("${shift.day}, ${shift.time} at ${shift.labName}",
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}