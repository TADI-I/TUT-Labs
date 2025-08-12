package com.tadiwanashe.tutictlabs.Views

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.tadiwanashe.tutictlabs.Models.LabStatus
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabStatusView(
    isAdmin: Boolean,
    viewModel: LabViewModel = viewModel(),
    navController: NavHostController
) {
    val labs = listOf("Lab 10 - 138", "Lab 10 - G10", "Lab 10 - G06")
    var selectedLab by remember { mutableStateOf(labs[0]) }
    var labName by remember { mutableStateOf("10-138") }
    var labOpen by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val labStatuses = remember { mutableStateMapOf<String, LabStatus>() }

    LaunchedEffect(selectedLab) {
        fetchStatus(selectedLab, labStatuses) { status ->
            status?.let {
                labOpen = it.isOpen
                note = it.note
            } ?: run {
                labOpen = false
                note = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isAdmin) "Update Lab Status" else "View Lab Status") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Lab", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    DropdownMenu(
                        selected = selectedLab,
                        onSelected = { selectedLab = it },
                        items = labs
                    )
                }
            }

            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Lab Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lab is Open")
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = labOpen,
                            onCheckedChange = { labOpen = it },
                            enabled = canEditStatus(
                                currentUserId,
                                isAdmin,
                                labStatuses[selectedLab]
                            )
                        )
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Optional note") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canEditStatus(
                            currentUserId,
                            isAdmin,
                            labStatuses[selectedLab]
                        )
                    )
                }
            }

            if (canEditStatus(currentUserId, isAdmin, labStatuses[selectedLab])) {
                Button(
                    onClick = {
                        updateStatus(
                            selectedLab,
                            labOpen,
                            note,
                            currentUserId,
                            isAdmin,
                            viewModel
                        ) { success ->
                            if (success) {
                                showConfirmation = true
                                fetchStatus(selectedLab, labStatuses) { status ->
                                    status?.let {
                                        labOpen = it.isOpen
                                        note = it.note
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Update Status")
                }
            }
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

@Composable
fun DropdownMenu(
    selected: String,
    onSelected: (String) -> Unit,
    items: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) }, // Correct DropdownMenuItem syntax
                    onClick = {
                        onSelected(item)
                        expanded = false
                    })
            }
        }
    }
}

private fun canEditStatus(
    currentUserId: String?,
    isAdmin: Boolean,
    status: LabStatus?
): Boolean {
    status ?: return true // No status yet, allow creation

    // If the lab is OPEN, only the person who opened it or admin can close it
    if (status.isOpen) {
        return status.updatedById == currentUserId || isAdmin
    }

    // If the lab is CLOSED, anyone (including different users) can open it
    return true
}

private fun fetchStatus(
    lab: String,
    labStatuses: MutableMap<String, LabStatus>,
    onComplete: (LabStatus?) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("labStatus")
        .document(lab)
        .get()
        .addOnSuccessListener { doc ->
            val data = doc.data
            if (data != null) {
                val status = LabStatus(
                    id = doc.id,
                    labName = lab,
                    isOpen = data["labOpen"] as? Boolean ?: false,
                    note = data["note"] as? String ?: "",
                    updatedBy = data["updatedBy"] as? String ?: "Unknown",
                    updatedById = data["updatedById"] as? String ?: "",
                    timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                )
                labStatuses[lab] = status
                onComplete(status)
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener {
            onComplete(null)
        }
}

private fun updateStatus(
    lab: String,
    isOpen: Boolean,
    note: String,
    currentUserId: String?,
    isAdmin: Boolean,
    viewModel: LabViewModel,
    onComplete: (Boolean) -> Unit
) {
    currentUserId ?: return onComplete(false)

    val data = hashMapOf(
        "labName" to lab,
        "labOpen" to isOpen,
        "note" to note,
        "updatedBy" to if (isAdmin) "Admin" else "Tutor",
        "updatedById" to currentUserId,
        "timestamp" to FieldValue.serverTimestamp()
    )

    FirebaseFirestore.getInstance()
        .collection("labStatus")
        .document(lab)
        .set(data)
        .addOnSuccessListener {
            if (isOpen) {
                viewModel.openLabSession(labName = lab, note = note)
            } else {
                viewModel.closeLabSession(labName = lab)
            }
            onComplete(true)
        }
        .addOnFailureListener {
            onComplete(false)
        }
}