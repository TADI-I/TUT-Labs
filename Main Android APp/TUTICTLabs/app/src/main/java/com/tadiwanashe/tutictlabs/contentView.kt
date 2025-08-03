package com.tadiwanashe.tutictlabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import com.tadiwanashe.tutictlabs.Views.AdminDashboardView
import com.tadiwanashe.tutictlabs.Views.LoginView
import com.tadiwanashe.tutictlabs.Views.StudentLabStatusView
import com.tadiwanashe.tutictlabs.Views.TutorDashboardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentView(
    authViewModel: AuthViewModel = remember { AuthViewModel() },
    labViewModel: LabViewModel = remember { LabViewModel() }
) {
    val navController = rememberNavController()
    var showStudentView by remember { mutableStateOf(false) }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TUT Labs") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { showStudentView = true }) {
                Text("📘 View Lab Info Without Login")
            }

            if (showStudentView) {
                StudentLabStatusView(labViewModel = labViewModel , navController = navController)
            }

            Divider()

            when {
                isLoggedIn -> {
                    if (userRole == "admin") {
                        AdminDashboardView(authViewModel = authViewModel, navController = navController)
                    } else {
                        TutorDashboardView(authViewModel = authViewModel, navController = navController)
                    }
                }
                else -> {
                    LoginView(authViewModel = authViewModel, onLoginSuccess = {
                        // Optional callback on login success
                    })
                }
            }
        }
    }
}

