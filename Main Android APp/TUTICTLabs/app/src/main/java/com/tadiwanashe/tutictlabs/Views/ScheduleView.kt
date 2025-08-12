package com.tadiwanashe.tutictlabs.Views

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh;
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView(
    viewModel: LabViewModel = viewModel(), // Assume contains fetchTutorWeeklyShifts and generateTutorWeeklySchedulePDF
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    navController: NavHostController
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    val currentTutorId = auth.currentUser?.uid
    val shifts by viewModel.shiftsForCurrentTutor.collectAsState()

    fun refreshData() {
        refreshing = true
        currentTutorId?.let { tutorId ->
            viewModel.fetchTutorWeeklyShifts(tutorId) { fetchedShifts ->
                viewModel._shiftsForCurrentTutor.value = fetchedShifts
                refreshing = false
            }
        } ?: run { refreshing = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Schedule") },
                actions = {
                    IconButton(
                        onClick = { refreshData() },
                        enabled = !refreshing
                    ) {
                        if (refreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = if (shifts.isEmpty()) Arrangement.Center else Arrangement.Top
            ) {
                if (shifts.isEmpty()) {
                    item {
                        Text(
                            text = "No shifts assigned.",
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(shifts) { shift ->
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${shift.day} - ${shift.labName}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = shift.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    currentTutorId?.let { tutorId ->
                        val calendar = Calendar.getInstance()
                        val today = calendar.time


                        calendar.firstDayOfWeek = Calendar.MONDAY
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                        val startOfWeek = calendar.time

                        calendar.add(Calendar.DAY_OF_YEAR, 6)
                        val endOfWeek = calendar.time

                        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                        val weekRange = "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}"

                        viewModel.fetchTutorWeeklyShifts(tutorId) { shifts ->
                            scope.launch {
                                val pdfUri = viewModel.generateTutorWeeklySchedulePDF(
                                    weekRange = weekRange,
                                    shifts = shifts,
                                    context = context
                                )


                                pdfUri?.let { uri ->
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share Schedule PDF")
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = shifts.isNotEmpty()
            ) {
                Text("Download Weekly Schedule")
            }
        }
    }

    LaunchedEffect(currentTutorId) {
        currentTutorId?.let { tutorId ->
            viewModel.fetchTutorWeeklyShifts(tutorId) { shifts ->
                viewModel._shiftsForCurrentTutor.value = shifts
            }
        }
    }


}

