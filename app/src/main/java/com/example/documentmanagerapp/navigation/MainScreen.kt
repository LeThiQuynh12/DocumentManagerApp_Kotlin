package com.example.documentmanagerapp.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = AuthViewModelFactory(context).create(AuthViewModel::class.java)

    // Kiểm tra trạng thái đăng nhập
    LaunchedEffect(authViewModel.user) {
        if (authViewModel.user.value == null) {
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding))
    }
}