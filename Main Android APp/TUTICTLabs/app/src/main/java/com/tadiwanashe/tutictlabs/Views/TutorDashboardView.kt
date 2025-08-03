package com.tadiwanashe.tutictlabs.Views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardView(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Dashboard") },
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
                    title = "My Schedule",
                    onClick = { navController.navigate("schedule") }
                )
            }
            item {
                NavigationCard(
                    title = "Update Lab Status",
                    onClick = { navController.navigate("labStatus") }
                )
            }
        }
    }
}