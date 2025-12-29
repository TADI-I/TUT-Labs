package com.tadiwanashe.tutictlabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel

import com.tadiwanashe.tutictlabs.Views.AdminDashboardView
import com.tadiwanashe.tutictlabs.Views.LabStatusView
import com.tadiwanashe.tutictlabs.Views.LoginScreen
import com.tadiwanashe.tutictlabs.Views.ManageTutorView
import com.tadiwanashe.tutictlabs.Views.ScheduleManagementView
import com.tadiwanashe.tutictlabs.Views.ScheduleView
import com.tadiwanashe.tutictlabs.Views.StudentLabStatusView
import com.tadiwanashe.tutictlabs.Views.TutorDashboardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentView(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    var showStudentView by remember { mutableStateOf(false) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Public access button
                Button(
                    onClick = { showStudentView = true },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("🖥️ View Available Labs")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                // Authenticated user routing
                when {
                    isLoggedIn -> {
                        when (userRole) {
                            "admin" -> AdminDashboardView(authViewModel = authViewModel, navController = navController)
                            else -> TutorDashboardView(authViewModel = authViewModel, navController = navController)
                        }
                    }
                    else -> LoginScreen(
                        onLoginSuccess = { /* Navigation handled by auth state */ },
                        viewModel = authViewModel
                    )
                }
            }

            // Footer
            Text(
                text = "Made by Tadiwanashe Songore For TUT FoICT",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 30.dp, bottom = 10.dp)
            )
        }
    }

    // Navigation setup
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("main") { /* Empty if you're handling content in Scaffold */ }
        composable("ScheduleView") {
            ScheduleView(navController = navController)
        }
        composable("LabStatusView") {
            LabStatusView(userRole=="admin",navController = navController)
        }
        composable("ManageTutorView") {
            ManageTutorView(navController = navController)
        }
        composable("ScheduleManagementView") {
            ScheduleManagementView(navController = navController)
        }

        // Add other destinations here
    }

    // Student view sheet
    if (showStudentView) {
        ModalBottomSheet(
            onDismissRequest = { showStudentView = false }
        ) {
            StudentLabStatusView()
        }
    }
}