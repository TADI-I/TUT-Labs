package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardView(
    authViewModel: AuthViewModel,
    //labViewModel: LabViewModel,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.Red
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                NavigationCard1(
                    title = "Manage Tutors",
                    onClick = { navController.navigate("manageTutors") }
                )
            }
            item {
                NavigationCard1(
                    title = "Manage Schedule",
                    onClick = { navController.navigate("manageSchedule") }
                )
            }
            item {
                NavigationCard1(
                    title = "Open/Close Labs",
                    onClick = { navController.navigate("labStatus") }
                )
            }
        }
    }
}

@Composable
fun NavigationCard1(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}