package com.example.documentmanagerapp.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.repository.CollectionsRepository
import kotlinx.coroutines.launch

@Composable
fun CollectionsScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val collectionsRepository = CollectionsRepository(context)
    val coroutineScope = rememberCoroutineScope()

    val user by authViewModel.user.observeAsState()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var documentCounts by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isMainBoosterExpanded by remember { mutableStateOf(true) }
    var isAnotherSavedListExpanded by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("asc") }
    var isGridView by remember { mutableStateOf(true) }
    var dialogCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val emojis = listOf("🌈", "😺", "🧠", "🛸")
    val anotherEmojis = listOf("✈️", "💼", "🎯")

    // Gọi API khi màn hình được focus
    LaunchedEffect(user) {
        user?.id?.let { userId ->
            try {
                val (newCategories, newCounts) = collectionsRepository.fetchData(userId)
                categories = newCategories
                documentCounts = newCounts
                error = null
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    // Hiển thị lỗi
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog chọn hành động
    dialogCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { dialogCategory = null },
            title = { Text(category.name) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            dialogCategory = null
                            navController.navigate("editCategory/${category.id}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sửa danh mục",
                            color = Color(0xFF1E3A8A),        // Màu xanh đậm
                            fontSize = 18.sp,                 // Kích thước chữ
                            fontWeight = FontWeight.Bold,     // In đậm
                            letterSpacing = 1.sp,             // Giãn cách chữ
                            textAlign = TextAlign.Center,     // Căn giữa (nếu cần)
                            modifier = Modifier.fillMaxWidth() // Kéo rộng để căn giữa hoạt động
                        )

                    }
                    TextButton(
                        onClick = {
                            dialogCategory = null
                            showDeleteDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Xóa danh mục",
                            color = Color(0xFFFF0000),        // Màu xanh đậm
                            fontSize = 18.sp,                 // Kích thước chữ
                            fontWeight = FontWeight.Bold,     // In đậm
                            letterSpacing = 1.sp,             // Giãn cách chữ
                            textAlign = TextAlign.Center,     // Căn giữa (nếu cần)
                            modifier = Modifier.fillMaxWidth() // Kéo rộng để căn giữa hoạt động
                        )

                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogCategory = null }) {
                    Text("Hủy", color = Color(0xFF1E3A8A), fontSize=18.sp,fontWeight=FontWeight.Bold)
                }
            }
        )
    }

    // Dialog xác nhận xóa
    if (showDeleteDialog && dialogCategory != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                dialogCategory = null
            },
            title = { Text("Xóa danh mục ${dialogCategory!!.name}") },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                collectionsRepository.deleteCategory(dialogCategory!!.id)
                                categories = categories.filter { it.id != dialogCategory!!.id }
                                Toast.makeText(context, "Xóa danh mục thành công", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            showDeleteDialog = false
                            dialogCategory = null
                        }
                    }
                ) {
                    Text("Xóa", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    dialogCategory = null
                }) {
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    if (loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFFF97316))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đang tải danh mục...",
                fontSize = 16.sp,
                color = Color(0xFF1E3A8A)
            )
        }
        return
    }

    if (categories.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Không có danh mục nào",
                fontSize = 16.sp,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("addCategory") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
            ) {
                Text("Thêm danh mục", color = Color.White)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collections",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { navController.navigate("addCategory") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                    IconButton(onClick = {
                        sortOrder = if (sortOrder == "asc") "desc" else "asc"
                        categories = categories.sortedBy { it.name }.let {
                            if (sortOrder == "desc") it.reversed() else it
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Sort",
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.rotate(90f)
                        )
                    }
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.GridOn else Icons.Default.List,
                            contentDescription = "Toggle View",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE6F0FA))
                    .clickable { navController.navigate("search") }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .padding(bottom = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier
                            .size(25.dp)
                            .padding(end = 6.dp)
                    )
                    BasicTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        enabled = false,
                        decorationBox = { innerTextField ->
                            Box {
                                Text(
                                    text = "Search your collection",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 18.sp
                                )
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // Error Message
            error?.let { errorText ->
                Text(
                    text = errorText,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            // Main Booster Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMainBoosterExpanded = !isMainBoosterExpanded }
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Main Booster",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand/Collapse",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isMainBoosterExpanded) 0f else 180f)
                )
            }
        }

        if (isMainBoosterExpanded) {
            val mainBoosterCategories = categories.filter { it.group == "MAIN_BOOSTER" }
            if (mainBoosterCategories.isEmpty()) {
                item {
                    Text(
                        text = "Không có danh mục Main Booster",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (isGridView) {
                items(mainBoosterCategories.chunked(2)) { rowCategories ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CollectionCard(
                                navController = navController,
                                category = category,
                                count = "${documentCounts[category.id] ?: 0} item${if (documentCounts[category.id] != 1) "s" else ""}",
                                emoji = emojis[categories.indexOf(category) % emojis.size],
                                onClick = {
                                    navController.navigate("documentList/${category.id}/${category.name}")
                                },
                                onEllipsisClick = { dialogCategory = category },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(mainBoosterCategories) { category ->
                    CollectionCard(
                        navController = navController,
                        category = category,
                        count = "${documentCounts[category.id] ?: 0} item${if (documentCounts[category.id] != 1) "s" else ""}",
                        emoji = emojis[categories.indexOf(category) % emojis.size],
                        onClick = {
                            navController.navigate("documentList/${category.id}/${category.name}")
                        },
                        onEllipsisClick = { dialogCategory = category },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }

        item {
            // Another Saved List Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAnotherSavedListExpanded = !isAnotherSavedListExpanded }
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Another Saved List",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand/Collapse",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (isAnotherSavedListExpanded) 0f else 180f)
                )
            }
        }

        if (isAnotherSavedListExpanded) {
            val anotherSavedListCategories = categories.filter { it.group == "ANOTHER_SAVED_LIST" }
            if (anotherSavedListCategories.isEmpty()) {
                item {
                    Text(
                        text = "Không có danh mục Another Saved List",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (isGridView) {
                items(anotherSavedListCategories.chunked(2)) { rowCategories ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CollectionCard(
                                navController = navController,
                                category = category,
                                count = "${documentCounts[category.id] ?: 0} item${if (documentCounts[category.id] != 1) "s" else ""}",
                                emoji = anotherEmojis[categories.indexOf(category) % anotherEmojis.size],
                                onClick = {
                                    navController.navigate("documentList/${category.id}/${category.name}")
                                },
                                onEllipsisClick = { dialogCategory = category },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(anotherSavedListCategories) { category ->
                    CollectionCard(
                        navController = navController,
                        category = category,
                        count = "${documentCounts[category.id] ?: 0} item${if (documentCounts[category.id] != 1) "s" else ""}",
                        emoji = anotherEmojis[categories.indexOf(category) % anotherEmojis.size],
                        onClick = {
                            navController.navigate("documentList/${category.id}/${category.name}")
                        },
                        onEllipsisClick = { dialogCategory = category },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionCard(
    navController: NavController,
    category: Category,
    count: String,
    emoji: String,
    onClick: () -> Unit,
    onEllipsisClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .size(width = 160.dp, height = 150.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1DC))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📁",
                    fontSize = 36.sp
                )
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = count,
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
            IconButton(
                onClick = { onEllipsisClick() },
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color(0xFF888888)
                )
            }
        }
    }
}