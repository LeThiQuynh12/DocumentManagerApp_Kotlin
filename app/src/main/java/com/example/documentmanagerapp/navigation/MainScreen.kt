package com.example.documentmanagerapp.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import androidx.compose.runtime.getValue

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = AuthViewModelFactory(context).create(AuthViewModel::class.java)
    val noBottomBarRoutes = listOf("login", "register", "forgetpassword")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Gọi đăng xuất ngay khi khởi động ứng dụng
//    LaunchedEffect(Unit) {
//        authViewModel.logout()
//    }

//    LaunchedEffect(Unit) {
//        val tokens = authViewModel.tokenManager.getTokens()
//        if (tokens?.accessToken != null) {
//            // Nếu có accessToken, gọi API lấy user từ server (hoặc dùng cache nếu bạn có)
//            authViewModel.fetchUserInfo()
//        } else {
//            // Không có token → chuyển tới login
//            navController.navigate("login") {
//                popUpTo("main") { inclusive = true }
//            }
//        }
//    }

    // Điều hướng tới login nếu chưa đăng nhập
    LaunchedEffect(authViewModel.user.value) {
        if (authViewModel.user.value == null) {
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute !in noBottomBarRoutes) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding))
    }
}
