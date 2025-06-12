package com.example.documentmanagerapp.components.Collections

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun FileDetailsScreen(navController: NavController, documentId: Long) {
    Text(
        text = "File Details Screen for Document ID: $documentId",
        modifier = Modifier.fillMaxSize()
    )
}