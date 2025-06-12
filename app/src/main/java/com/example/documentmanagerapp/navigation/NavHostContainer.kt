package com.example.documentmanagerapp.components

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.documentmanagerapp.components.Bookmarks.BookmarksScreen
import com.example.documentmanagerapp.components.Collections.DocumentListScreen
import com.example.documentmanagerapp.components.Collections.FileDetailsScreen
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
                navController = navController,
                categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull() ?: -1L
            )
        }
        composable("documentList/{categoryId}/{categoryName}") { backStackEntry ->
            DocumentListScreen(
                navController = navController,
                categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull(),
                categoryName = backStackEntry.arguments?.getString("categoryName")
            )
        }
        composable("fileDetails/{documentId}") { backStackEntry ->
            FileDetailsScreen(
                navController = navController,
                documentId = backStackEntry.arguments?.getString("documentId")?.toLongOrNull() ?: -1L
            )
        }
    }
}