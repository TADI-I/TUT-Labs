package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tadiwanashe.tutictlabs.Models.LabShift
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.Models.User
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLabStatusView(
    labViewModel: LabViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val labStatuses by labViewModel.labStatuses.collectAsState()
    val labShifts by labViewModel.labShifts.collectAsState()
    val tutors by labViewModel.tutors.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lab Info") },
                actions = {
                    IconButton(
                        onClick = {
                            labViewModel.fetchShifts()
                            labViewModel.fetchTutors()
                            labViewModel.fetchLabStatuses()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    SectionHeader("🟢 Current Lab Statuses")
                }

                items(labStatuses) { status ->
                    LabStatusCard(status)
                }

                item {
                    SectionHeader("📅 Lab Roster")
                }

                items(labShifts) { shift ->
                    val tutor = tutors.firstOrNull { it.id == shift.tutorId }
                    LabShiftCard(shift, tutor)
                }
            }

            Footer()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun LabStatusCard(status: LabStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = status.labName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (status.isOpen) "Open" else "Closed",
                    color = if (status.isOpen) Color.Green else Color.Red
                )
            }

            if (status.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${status.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Updated: ${status.timestamp}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun LabShiftCard(shift: LabShift, tutor: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Lab: ${shift.labName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Time: ${shift.day}, ${shift.time}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tutor: ${tutor?.name ?: "Not assigned"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Made by Tadiwanashe Songore For TUT FoICT",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}