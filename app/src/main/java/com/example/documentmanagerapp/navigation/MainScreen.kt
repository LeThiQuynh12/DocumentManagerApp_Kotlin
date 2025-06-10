import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.documentmanagerapp.components.BottomNavigationBar
import com.example.documentmanagerapp.components.api.SecureStorage

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    val currentRoute by navController.currentBackStackEntryAsState()

    // Kiểm tra token khi Composable được tạo
    LaunchedEffect(Unit) {
        val token = SecureStorage.getToken()
        if (token != null) {
            isLoggedIn = true
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Chỉ hiển thị BottomNavigationBar nếu đã đăng nhập và không ở màn hình Login
            if (isLoggedIn && currentRoute?.destination?.route != "login") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding))
    }
}