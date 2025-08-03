package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabStatusView(
    isAdmin: Boolean,
    labViewModel: LabViewModel
) {
    val labs = listOf("Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06")
    var selectedLab by remember { mutableStateOf(labs[0]) }
    var labOpen by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    val currentUserId = Firebase.auth.currentUser?.uid
    var labStatuses by remember { mutableStateOf(mapOf<String, LabStatus>()) }

    // ✅ Move canEditStatus inside the Composable
    val canEditStatus = {
        val status = labStatuses[selectedLab]
        when {
            status == null -> true
            status.isOpen -> status.updatedById == currentUserId || isAdmin
            else -> true
        }
    }

    LaunchedEffect(Unit) {
        labViewModel.fetchLabStatuses()
    }

    val statuses by labViewModel.labStatuses.collectAsState()

    LaunchedEffect(selectedLab, statuses) {
        labStatuses = statuses.associateBy { it.labName }
        labStatuses[selectedLab]?.let {
            labOpen = it.isOpen
            note = it.note
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (isAdmin) "Admin Announcements" else "Update Lab Status",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lab Selection
        Text("Select Lab", style = MaterialTheme.typography.labelMedium)
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedLab,
                onValueChange = {},
                label = { Text("Select Lab") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                labs.forEach { lab ->
                    DropdownMenuItem(
                        text = { Text(lab) },
                        onClick = {
                            selectedLab = lab
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lab Status
        Text("Lab Status", style = MaterialTheme.typography.labelMedium)
        Switch(
            checked = labOpen,
            onCheckedChange = { if (canEditStatus()) labOpen = it },
            enabled = canEditStatus()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Note Field
        OutlinedTextField(
            value = note,
            onValueChange = { if (canEditStatus()) note = it },
            label = { Text("Optional note") },
            enabled = canEditStatus(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (canEditStatus()) {
            Button(
                onClick = {
                    labViewModel.updateLabStatus(
                        labName = selectedLab,
                        isOpen = labOpen,
                        note = note,
                        isAdmin = isAdmin
                    )
                    showConfirmation = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Status")
            }
        }

        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { showConfirmation = false },
                title = { Text("Status Updated!") },
                confirmButton = {
                    Button(onClick = { showConfirmation = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
