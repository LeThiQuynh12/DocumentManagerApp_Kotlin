import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.documentmanagerapp.components.AddFileScreen
import com.example.documentmanagerapp.components.Home.HomeScreen

import com.example.documentmanagerapp.components.SearchScreen
import com.example.documentmanagerapp.components.Setting.SettingScreen

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier) {
    NavHost(navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController) }

        composable("bookmarks") { Text("Bookmarks Screen") }
        composable("search") { SearchScreen() }
        composable("add") { AddFileScreen() }

        composable("collections") { CollectionsScreen(navController) }
        // composable("settings") { Text("Settings Screen") }
        composable("settings") { SettingScreen(navController) }

        composable("login") {
            LoginScreen(navController)
        }

    }
}
