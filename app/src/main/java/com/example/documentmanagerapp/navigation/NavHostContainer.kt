package com.example.documentmanagerapp.components

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.documentmanagerapp.components.Bookmarks.BookmarksScreen

import com.example.documentmanagerapp.components.Home.HomeScreen
import com.example.documentmanagerapp.components.Setting.SettingScreen

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = "login", modifier = modifier) {
        composable("login") { LoginScreen(navController) }
        composable("home") {
            HomeScreen(
                navController = navController,
                onSearchClick = { navController.navigate("search") }
            )
        }


        composable("bookmarks") { BookmarksScreen(navController) }
        composable("search") { SearchScreen() }
        composable("add") { AddFileScreen() }
        composable("collections") { CollectionsScreen(navController) }
        composable("settings") { SettingScreen(navController) }
        composable("addCategory") { AddCategoryScreen(navController) }
        composable("editCategory/{categoryId}") { backStackEntry ->
            EditCategoryScreen(
                navController,
                backStackEntry.arguments?.getString("categoryId")?.toLong() ?: -1
            )
        }
        composable("documentList/{categoryId}/{categoryName}") { backStackEntry ->
            DocumentListScreen(
                navController,
                categoryId = backStackEntry.arguments?.getString("categoryId")?.toLong(),
                categoryName = backStackEntry.arguments?.getString("categoryName")
            )
        }
    }
}