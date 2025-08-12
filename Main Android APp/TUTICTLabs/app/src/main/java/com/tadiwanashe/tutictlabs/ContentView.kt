package com.tadiwanashe.tutictlabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tadiwanashe.tutictlabs.ViewModels.AuthViewModel
import com.tadiwanashe.tutictlabs.ViewModels.LabViewModel
import com.tadiwanashe.tutictlabs.Views.AdminDashboardView
import com.tadiwanashe.tutictlabs.Views.LoginScreen
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

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Authenticated user routing
                when {
                    isLoggedIn -> {
                        when (userRole) {
                            "admin" -> AdminDashboardView(authViewModel = authViewModel,navController = navController)
                            else -> TutorDashboardView(authViewModel = authViewModel ,navController = navController)
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

    // Student view sheet
    if (showStudentView) {
        ModalBottomSheet(
            onDismissRequest = { showStudentView = false }
        ) {
            // StudentLabStatusView()
        }
    }
}