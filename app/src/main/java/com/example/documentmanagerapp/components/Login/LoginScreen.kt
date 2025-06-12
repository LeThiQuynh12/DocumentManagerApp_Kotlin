

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.*
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

//
//@Composable
//fun LoginScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
//    val coroutineScope = rememberCoroutineScope()
//    val handleSignUp = { navController.navigate("register") }
//
//    // Khai báo state cho email và password
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//
//    // Quan sát LiveData với observeAsState
//    val user by authViewModel.user.observeAsState(initial = null)
//    val loading by authViewModel.loading.observeAsState(initial = false)
//    val error by authViewModel.error.observeAsState(initial = null)
//
//    // Hiệu ứng điều hướng và thông báo khi đăng nhập thành công
//    LaunchedEffect(user) {
//        if (user != null) {
//            Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
//            navController.navigate("home") {
//                popUpTo(0) { inclusive = true } // Xóa backstack
//            }
//        }
//    }
//
//    // Hiển thị thông báo lỗi
//    LaunchedEffect(error) {
//        error?.let { errorMessage ->
//            Toast.makeText(context, errorMessage as CharSequence, Toast.LENGTH_LONG).show()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = 16.dp, vertical = 40.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Title
//        Text(
//            text = "Đăng Nhập",
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1E3A8A),
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        Text(
//            text = "Đăng nhập với tài khoản hoặc bookmark của bạn",
//            fontSize = 14.sp,
//            color = Color(0xFF666666),
//            modifier = Modifier.padding(bottom = 32.dp)
//        )
//
//        // Email Field
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.MailOutline,
//                    contentDescription = "Email Icon",
//                    tint = Color(0xFF1E90FF)
//                )
//            },
//            label = { Text("Email") },
//            placeholder = { Text("Nhập email") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            singleLine = true,
//            enabled = !loading
//        )
//
//        // Password Field
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            leadingIcon = {
//                Icon(
//                    imageVector = Icons.Default.Lock,
//                    contentDescription = "Password Icon",
//                    tint = Color(0xFF1E90FF)
//                )
//            },
//            label = { Text("Mật khẩu") },
//            placeholder = { Text("Nhập mật khẩu") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//            singleLine = true,
//            enabled = !loading
//        )
//
//        // Forgot Password
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Text(
//                text = "Bạn quên mật khẩu?",
//                fontSize = 12.sp,
//                color = Color.Black
//            )
//
//            Text(
//                text = " Đặt lại mật khẩu",
//                fontSize = 12.sp,
//                color = Color(0xFF1E90FF),
//                modifier = Modifier.clickable {
//                    navController.navigate("forgetpassword")
//                }
//            )
//        }
//
//
//
//        // Sign Up Link
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 24.dp),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "Bạn chưa có tài khoản?",
//                fontSize = 12.sp,
//                color = Color.Black
//            )
//            Spacer(modifier = Modifier.width(4.dp)) // khoảng cách nhỏ giữa 2 Text
//            Text(
//                text = "Đăng ký tài khoản",
//                fontSize = 12.sp,
//                color = Color(0xFF1E90FF),
//                modifier = Modifier.clickable {
//                    handleSignUp()
//                }
//            )
//        }
//
//
//        // Login Button
//        Button(
//            onClick = {
//                if (email.isBlank() || password.isBlank()) {
//                    Toast.makeText(context, "Thiếu trường dữ liệu", Toast.LENGTH_SHORT).show()
//                } else {
//                    coroutineScope.launch {
//                        authViewModel.login(email, password)
//                    }
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
//            enabled = !loading
//        ) {
//            if (loading == true) {
//                CircularProgressIndicator(
//                    color = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            } else {
//                Text(
//                    text = "Đăng nhập",
//                    color = Color.White,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val user by authViewModel.user.observeAsState(initial = null)
    val loading by authViewModel.loading.observeAsState(initial = false)
    val error by authViewModel.error.observeAsState(initial = null)

    var showPassword by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(user) {
        if (user != null) {
            Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1E3A8A)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chào mừng trở lại!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        Text(
            text = "Đăng nhập với tài khoản của bạn để tiếp tục",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Nhập email") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !loading
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            placeholder = { Text("Nhập mật khẩu") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !loading
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Quên mật khẩu?",
                fontSize = 12.sp,
                color = Color(0xFF1E3A8A),
                modifier = Modifier.clickable {
                    navController.navigate("forgetpassword")
                }
            )
        }

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Thiếu trường dữ liệu", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch {
                        authViewModel.login(email, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(12.dp),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Đăng Nhập",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Bạn chưa có tài khoản?")
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đăng ký ngay",
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}
