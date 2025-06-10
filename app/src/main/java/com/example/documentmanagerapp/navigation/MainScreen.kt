import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.documentmanagerapp.components.BottomNavigationBar
import com.example.documentmanagerapp.components.api.ApiClient
import com.example.documentmanagerapp.components.api.Auth.AuthApiService
import com.example.documentmanagerapp.components.api.Data.request.LoginRequestDTO
import com.example.documentmanagerapp.components.api.Service.loginAndSaveToken

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // ✅ Gọi login khi Composable được tạo
    LaunchedEffect(Unit) {
        val retrofit = ApiClient.create { null } // chưa có token
        val authApi = retrofit.create(AuthApiService::class.java)

        val loginRequest = LoginRequestDTO(
            username = "user@example.com",  // 🔁 sửa lại đúng tên field nếu backend yêu cầu là "username"
            password = "password123"
        )

        loginAndSaveToken(
            authApi = authApi,
            loginRequest = loginRequest,
            onSuccess = {
                println("✅ Đăng nhập thành công và lưu token")
            },
            onError = {
                println("❌ Lỗi đăng nhập: ${it.message}")
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        NavHostContainer(navController, Modifier.padding(it))
    }
}
