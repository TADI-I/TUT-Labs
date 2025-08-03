package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.Models.LabShift


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLabStatusView(
    labViewModel: LabViewModel,
    navController: NavController
) {
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        labViewModel.fetchShifts()
        labViewModel.fetchTutors()
        labViewModel.fetchLabStatuses()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lab Info") })
        },
        bottomBar = {
            // Add a bottom bar with the Login button
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = {
                        navController.navigate("login")
                    }
                ) {
                    Text("Login")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Current Lab Statuses
            item {
                Text(
                    "🟢 Current Lab Statuses",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(labViewModel.labStatuses.value) { status ->
                LabStatusCard(status = status)
            }

            // Lab Roster
            item {
                Text(
                    "📅 Lab Roster",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(labViewModel.labShifts.value) { shift ->
                val tutor = labViewModel.tutors.value.find { it.id == shift.tutorId }
                LabShiftCard(shift = shift, tutorName = tutor?.name ?: "Unknown")
            }
        }
    }
}

@Composable
fun LabStatusCard(status: LabStatus) {
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(status.labName, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (status.isOpen) "Open" else "Closed",
                    color = if (status.isOpen) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            if (status.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: ${status.note}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Updated: ${dateFormat.format(status.timestamp)}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun LabShiftCard(shift: LabShift, tutorName: String) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lab: ${shift.labName}", style = MaterialTheme.typography.titleSmall)
            Text("Time: ${shift.day}, ${shift.time}", style = MaterialTheme.typography.bodySmall)
            Text("Tutor: $tutorName", style = MaterialTheme.typography.bodySmall)
        }
    }
}