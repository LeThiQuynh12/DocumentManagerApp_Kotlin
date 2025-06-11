package com.example.documentmanagerapp.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun AddCategoryScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = CollectionsRepository(context)
    val coroutineScope = rememberCoroutineScope()
    var categoryName by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("MAIN_BOOSTER") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Thêm Danh Mục",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(bottom = 20.dp)
        )

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
            RadioButton(
                selected = selectedGroup == "MAIN_BOOSTER",
                onClick = { selectedGroup = "MAIN_BOOSTER" }
            )
            Text(
                text = "Main Booster",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            )
            RadioButton(
                selected = selectedGroup == "ANOTHER_SAVED_LIST",
                onClick = { selectedGroup = "ANOTHER_SAVED_LIST" }
            )
            Text(
                text = "Another Saved List",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            )
        }

        Button(
            onClick = {
                if (categoryName.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch {
                        try {
                            repository.addCategory(categoryName, selectedGroup)
                            Toast.makeText(context, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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
                text = "Thêm",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}