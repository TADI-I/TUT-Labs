package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@Composable
fun ScheduleView(
    viewModel: LabViewModel
) {
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchShiftsForCurrentTutor()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.shiftsForCurrentTutor.value.isEmpty()) {
            Text(
                text = "No shifts assigned.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(viewModel.shiftsForCurrentTutor.value) { shift ->
                    Card(modifier = Modifier.padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${shift.day} - ${shift.labName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = shift.time,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}