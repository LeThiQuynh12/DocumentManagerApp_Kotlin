package com.example.documentmanagerapp.components

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.documentmanagerapp.components.Collections.DocumentListScreen
import com.example.documentmanagerapp.components.Collections.FileDetailsScreen
import com.example.documentmanagerapp.components.Home.HomeScreen
import com.example.documentmanagerapp.components.Login.ForgetPasswordScreen
import com.example.documentmanagerapp.components.Login.RegisterScreen
import com.example.documentmanagerapp.components.Setting.SettingScreen
import com.example.documentmanagerapp.components.Setting.UsedSpaceScreen

import com.example.documentmanagerapp.screens.BookmarkScreen

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
        composable("bookmarks") { BookmarkScreen(navController) }
        composable("search") { SearchScreen(navController) }
        composable("add") { AddFileScreen(navController) }
        composable("collections") { CollectionsScreen(navController) }
        composable("settings") { SettingScreen(navController) }

        composable("used_space") { UsedSpaceScreen(navController) }

        composable("addCategory") { AddCategoryScreen(navController) }
        composable("editCategory/{categoryId}") { backStackEntry ->
            EditCategoryScreen(
                navController = navController,
                categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull() ?: -1L
            )
        }
        composable("forgetpassword") { ForgetPasswordScreen(navController) }
        composable("register") { RegisterScreen(navController) }
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