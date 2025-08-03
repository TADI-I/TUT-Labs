package com.tadiwanashe.tutictlabs.Views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

import com.tadiwanashe.tutictlabs.Models.User

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTutorsView(
    viewModel: LabViewModel
) {
    var newTutorName by remember { mutableStateOf("") }
    var newTutorEmail by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    val tutors by viewModel.tutors.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTutors()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Add New Tutor Section
        Card(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Tutor", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTutorName,
                    onValueChange = { newTutorName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTutorEmail,
                    onValueChange = { newTutorEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (!isValidEmail(newTutorEmail)) {
                            alertMessage = "Please enter a valid email address."
                            showAlert = true
                            return@Button
                        }

                        if (tutors.any { it.email.equals(newTutorEmail, ignoreCase = true) }) {
                            alertMessage = "A tutor with this email already exists."
                            showAlert = true
                            return@Button
                        }

                        if (newTutorName.trim().isEmpty()) {
                            alertMessage = "Tutor name cannot be empty."
                            showAlert = true
                            return@Button
                        }

                        viewModel.addTutor(newTutorName, newTutorEmail)
                        newTutorName = ""
                        newTutorEmail = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Tutor")
                }
            }
        }

        // Current Tutors List
        Text(
            text = "Current Tutors",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        when {
            viewModel.tutors.value.isEmpty() -> {
                Text(
                    text = "No tutors found",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn {
                    items(viewModel.tutors.value) { tutor ->
                        TutorItem(
                            tutor = tutor,
                            onDelete = { viewModel.removeTutor(tutor.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Validation Error") },
            text = { Text(alertMessage) },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun TutorItem(tutor: User, onDelete: () -> Unit) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tutor.name, style = MaterialTheme.typography.titleSmall)
                Text(tutor.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }

    }

}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}