package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardView(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(
                        onClick = { authViewModel.logout() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                NavigationCard(
                    title = "Manage Tutors",
                    onClick = { navController.navigate("manageTutors") }
                )
            }
            item {
                NavigationCard(
                    title = "Manage Schedule",
                    onClick = { navController.navigate("scheduleManagement") }
                )
            }
            item {
                NavigationCard(
                    title = "Send Announcement",
                    onClick = { navController.navigate("labStatus/admin") }
                )
            }
        }
    }
}

@Composable
fun NavigationCard(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}