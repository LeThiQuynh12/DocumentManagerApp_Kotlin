import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.documentmanagerapp.components.Home.HomeScreen

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier) {
    NavHost(navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen() }
        composable("bookmarks") { Text("Bookmarks Screen") }
        composable("add") { Text("Add Screen") }
        composable("collections") { Text("Collections Screen") }
        composable("settings") { Text("Settings Screen") }
    }
}
