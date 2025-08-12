package com.tadiwanashe.tutictlabs.Views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import kotlinx.coroutines.delay

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTutorView(
    viewModel: LabViewModel = viewModel(),
    navController: NavHostController
) {
    val tutors by viewModel.tutors.collectAsState()
    //val isLoading by viewModel.isLoading.collectAsState()

    var newTutorName by remember { mutableStateOf("") }
    var newTutorEmail by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }



    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Tutors") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Tutor", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTutorName,
                        onValueChange = { newTutorName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTutorEmail,
                        onValueChange = { newTutorEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            autoCorrect = false
                        )
                    )

                    Button(
                        onClick = {
                            if (!isValidEmail(newTutorEmail)) {
                                alertMessage = "Please enter a valid email address."
                                showAlert = true
                                return@Button
                            }

                            if (viewModel.tutors.value.any { it.email.equals(newTutorEmail, ignoreCase = true) }) {
                                alertMessage = "A tutor with this email already exists."
                                showAlert = true
                                return@Button
                            }

                            if (newTutorName.trim().isEmpty()) {
                                alertMessage = "Tutor name cannot be empty."
                                showAlert = true
                                return@Button
                            }

                            viewModel.addTutor(name = newTutorName, email = newTutorEmail)
                            newTutorName = ""
                            newTutorEmail = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Tutor")
                    }
                }
            }

            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Tutors", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tutors.isEmpty()) {
                        Text(
                            text = "No tutors found",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn {
                            items(tutors) { tutor ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tutor.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = tutor.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeTutor(tutorId = tutor.id) },
                                        modifier = Modifier.size(24.dp)
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
    }

    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.fetchTutors()
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

private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email)
}
