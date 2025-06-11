package com.example.documentmanagerapp.components.Setting

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
@Composable
fun SettingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val coroutineScope = rememberCoroutineScope()
    val user by authViewModel.user.observeAsState(initial = null)
    val error by authViewModel.error.observeAsState(initial = null)

    // Hiển thị thông báo lỗi nếu có
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Admin Section
        SettingItem(
            icon = Icons.Default.Person,
            title = "Quản trị viên",
            subtitle = user?.email ?: "admin@admin.com",
            onClick = { /* Handle click */ }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Data Section
        Text(
            text = "Data",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SettingItem(
            icon = Icons.Default.ArrowForward,
            title = "Import / Export",
            onClick = { /* Handle click */ }
        )
        SettingItem(
            icon = Icons.Default.Cloud,
            title = "Cloud Backup",
            onClick = { /* Handle click */ }
        )
        SettingItem(
            icon = Icons.Default.Storage,
            title = "Used Space",
            onClick = { /* Handle click */ }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // More Section
        Text(
            text = "More",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SettingItem(
            icon = Icons.Default.Help,
            title = "Help",
            onClick = { /* Handle click */ }
        )
        SettingItem(
            icon = painterResource(android.R.drawable.ic_menu_info_details),
            title = "Terms & Conditions",
            onClick = { /* Handle click */ }
        )
        SettingItem(
            icon = Icons.Default.Lock,
            title = "Privacy & Policy",
            onClick = { /* Handle click */ }
        )

        // Logout Button
        Spacer(modifier = Modifier.weight(1f))
        if (user != null) { // Chỉ hiển thị nút khi người dùng đã đăng nhập
            Button(
                onClick = {
                    coroutineScope.launch {
                        authViewModel.logout() // Xóa token và dữ liệu người dùng
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // Xóa sạch back stack
                            launchSingleTop = true
                        }
                        Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4500)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: Any,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (icon) {
            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF1E90FF),
                    modifier = Modifier.size(24.dp)
                )
            }
            is Painter -> {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Color(0xFF1E90FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF888888)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color(0xFF888888),
            modifier = Modifier.size(20.dp)
        )
    }
}