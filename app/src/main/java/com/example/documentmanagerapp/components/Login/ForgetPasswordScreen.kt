package com.example.documentmanagerapp.components.Login
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
@Composable
fun ForgetPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E3A8A)
                )
            }
        }

        // Title
        Text(
            text = "Khôi phục mật khẩu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.Start)
        )

        // Subtitle
        Text(
            text = "Nhập vào email liên kết với tài khoản của bạn và chúng tôi sẽ gửi hướng dẫn lấy lại mật khẩu tới tài khoản của bạn",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.Start)
        )

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = "Email Icon",
                    tint = Color(0xFF1E90FF)
                )
            },
            label = { Text("Email") },
            placeholder = { Text("Nhập email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            singleLine = true
        )

        // Gửi button
        Button(
            onClick = {
                if (email.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                } else {
                    // TODO: Gửi yêu cầu khôi phục mật khẩu
                    Toast.makeText(context, "Đã gửi hướng dẫn tới email", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Gửi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
