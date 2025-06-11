package com.example.documentmanagerapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DocumentListScreen(navController: NavController, categoryId: Long?, categoryName: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách tài liệu: $categoryName (ID: $categoryId)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        // TODO: Thêm logic để lấy và hiển thị danh sách tài liệu
    }
}