package com.example.documentmanagerapp.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentmanagerapp.utils.repository.CollectionsRepository
import kotlinx.coroutines.launch

@Composable
fun EditCategoryScreen(navController: NavController, categoryId: Long) {
    val context = LocalContext.current
    val repository = CollectionsRepository(context)
    val coroutineScope = rememberCoroutineScope()
    var categoryName by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("MAIN_BOOSTER") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Lấy userId từ SharedPreferences
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("user_id", 1L) // Cần lấy từ AuthViewModel

    // Tải dữ liệu danh mục
    LaunchedEffect(categoryId) {
        if (categoryId <= 0) {
            Log.e("EditCategoryScreen", "Invalid categoryId: $categoryId")
            Toast.makeText(context, "Danh mục không hợp lệ", Toast.LENGTH_SHORT).show()
            navController.navigate("collections") {
                popUpTo("collections") { inclusive = false }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        try {
            Log.d("EditCategoryScreen", "Fetching category with ID: $categoryId")
            val category = repository.getCategoryById(categoryId)
            if (category != null) {
                categoryName = category.name
                selectedGroup = category.group
                Log.d("EditCategoryScreen", "Category loaded: $category")
            } else {
                Log.w("EditCategoryScreen", "Category not found for ID: $categoryId")
                error = "Không tìm thấy danh mục"
                Toast.makeText(context, "Không tìm thấy danh mục", Toast.LENGTH_SHORT).show()
                navController.navigate("collections") {
                    popUpTo("collections") { inclusive = false }
                    launchSingleTop = true
                }
            }
        } catch (e: Exception) {
            Log.e("EditCategoryScreen", "Error fetching category: ${e.message}")
            error = "Lỗi: ${e.message}"
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            navController.navigate("collections") {
                popUpTo("collections") { inclusive = false }
                launchSingleTop = true
            }
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thanh tiêu đề với nút back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("collections") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = "Sửa danh mục",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            // Spacer để căn chỉnh
            IconButton(onClick = {}, enabled = false) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.Transparent
                )
            }
        }

        if (isLoading) {
            Text(
                text = "Đang tải...",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else if (error != null) {
            Text(
                text = error!!,
                fontSize = 16.sp,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Tên danh mục") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = selectedGroup == "MAIN_BOOSTER",
                        onClick = { selectedGroup = "MAIN_BOOSTER" }
                    )
                    Text(
                        text = "Main Booster",
                        fontSize = 16.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = selectedGroup == "ANOTHER_SAVED_LIST",
                        onClick = { selectedGroup = "ANOTHER_SAVED_LIST" }
                    )
                    Text(
                        text = "Another Saved List",
                        fontSize = 16.sp
                    )
                }
            }

            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedCategory = repository.updateCategory(categoryId, categoryName, selectedGroup, userId)
                                Toast.makeText(context, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show()
                                navController.navigate("collections") {
                                    popUpTo("collections") { inclusive = false }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Log.e("EditCategoryScreen", "Error updating category: ${e.message}")
                                Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
            ) {
                Text(
                    text = "Cập nhật",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}