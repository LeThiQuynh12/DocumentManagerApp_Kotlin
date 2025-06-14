package com.example.documentmanagerapp.components

import android.content.Context
import android.util.Log
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.repository.CollectionsRepository
import kotlinx.coroutines.launch

// ViewModel để quản lý logic và trạng thái
class CollectionsViewModel(
    private val repository: CollectionsRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var documentCounts by mutableStateOf<Map<Long, Int>>(emptyMap())
        private set
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun fetchData(userId: Long) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val (newCategories, newCounts) = repository.fetchData(userId)
                categories = newCategories
                documentCounts = newCounts
                Log.d("CollectionsViewModel", "Fetched ${categories.size} categories")
            } catch (e: Exception) {
                error = e.message
                Log.e("CollectionsViewModel", "Error fetching data: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun deleteCategory(categoryId: Long, userId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(categoryId, userId)
                categories = categories.filter { it.id != categoryId }
                fetchData(userId) // Làm mới danh sách
                onSuccess()
                Log.d("CollectionsViewModel", "Deleted category: $categoryId")
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi xóa danh mục không xác định")
                Log.e("CollectionsViewModel", "Error deleting category: ${e.message}")
            }
        }
    }

    fun checkCategoryExists(categoryId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val category = repository.getCategoryById(categoryId)
                onResult(category != null)
            } catch (e: Exception) {
                onResult(false)
                Log.e("CollectionsViewModel", "Error checking category: ${e.message}")
            }
        }
    }

    fun sortCategories(order: String) {
        categories = categories.sortedBy { it.name }.let {
            if (order == "desc") it.reversed() else it
        }
    }
}

class CollectionsViewModelFactory(
    private val repository: CollectionsRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectionsViewModel::class.java)) {
            return CollectionsViewModel(repository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun CollectionsScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val repository = CollectionsRepository(context)
    val viewModel: CollectionsViewModel = viewModel(
        factory = CollectionsViewModelFactory(repository, authViewModel)
    )
    val user by authViewModel.user.observeAsState()
    val categories by remember { derivedStateOf { viewModel.categories } }
    val documentCounts by remember { derivedStateOf { viewModel.documentCounts } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val error by remember { derivedStateOf { viewModel.error } }

    var isMainBoosterExpanded by remember { mutableStateOf(true) }
    var isAnotherSavedListExpanded by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("asc") }
    var isGridView by remember { mutableStateOf(true) }
    var dialogCategory by remember { mutableStateOf<Category?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val emojis = listOf("🌈", "😺", "🧠", "🛸")
    val anotherEmojis = listOf("✈️", "💼", "🎯")

    // Tải dữ liệu khi có userId, không điều hướng nếu không có user
    LaunchedEffect(user?.id) {
        Log.d("CollectionsScreen", "User ID: ${user?.id}")
        user?.id?.let { userId ->
            viewModel.fetchData(userId)
        } ?: run {
//            viewModel.error = "Không tìm thấy người dùng"
            viewModel.loading = false
//            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }

    // Xử lý lỗi, không điều hướng đến login
    LaunchedEffect(error) {
        error?.let {
            val message = when {
                it.contains("liên kết") -> "Không thể xóa danh mục vì còn tài liệu liên kết"
                it.contains("Phiên đăng nhập hết hạn") -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                else -> "Lỗi: $it"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            // Không gọi logout hoặc navigate đến login
        }
    }

    // Sắp xếp danh mục
    LaunchedEffect(sortOrder) {
        viewModel.sortCategories(sortOrder)
    }

    // Dialog chọn hành động
    dialogCategory?.let { category ->
        AlertDialog(
            onDismissRequest = {
                dialogCategory = null
                showDeleteDialog = false // Reset cả showDeleteDialog
            },
            title = { Text(category.name) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.checkCategoryExists(category.id) { exists ->
                                    if (exists) {
                                        dialogCategory = null
                                        showDeleteDialog = false
                                        navController.navigate("editCategory/${category.id}")
                                    } else {
                                        dialogCategory = null
                                        showDeleteDialog = false
                                        Toast.makeText(context, "Danh mục không tồn tại", Toast.LENGTH_SHORT).show()
                                        user?.id?.let { viewModel.fetchData(it) }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Edit category",
                            color = Color(0xFF1E3A8A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(
                        onClick = {
                            showDeleteDialog = true // Chỉ đặt showDeleteDialog, giữ dialogCategory
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Delete category",
                            color = Color(0xFFFF0000),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogCategory = null
                    showDeleteDialog = false
                }) {
                    Text("Cancel", color = Color(0xFF1E3A8A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            title = { Text("Delete category ${dialogCategory!!.name}") },
            text = { Text("Are you sure you want to delete this category? If the category has documents, you will need to delete the documents first.") },


            confirmButton = {
                TextButton(
                    onClick = {
                        user?.id?.let { userId ->
                            viewModel.deleteCategory(
                                categoryId = dialogCategory!!.id,
                                userId = userId,
                                onSuccess = {
                                    Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show()
                                    showDeleteDialog = false
                                    dialogCategory = null
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, "Lỗi xóa danh mục: $errorMessage", Toast.LENGTH_LONG).show()
                                    showDeleteDialog = false
                                    dialogCategory = null
                                }
                            )
                        } ?: run {
                            Toast.makeText(context, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_LONG).show()
                            showDeleteDialog = false
                            dialogCategory = null
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFF1E90FF))
                }
            },

            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    dialogCategory = null
                }) {
                    Text("Cancel", color = Color(0xFF1E3A8A))
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
                text = "Loading list...",
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
                text = "No categories yet",
                fontSize = 16.sp,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("addCategory") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
            ) {
                Text("Add category", color = Color.White)
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
                    IconButton(onClick = { sortOrder = if (sortOrder == "asc") "desc" else "asc" }) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE6F0FA))
                    .clickable { navController.navigate("search") }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .padding(bottom = 1.dp)
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
                        text = "No Main Booster category",
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
                                count = "${documentCounts[category.id] ?: 0} item${if ((documentCounts[category.id] ?: 0) != 1) "s" else ""}",
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
                        count = "${documentCounts[category.id] ?: 0} item${if ((documentCounts[category.id] ?: 0) != 1) "s" else ""}",
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
                        text = "No category Another Saved List",
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
                                count = "${documentCounts[category.id] ?: 0} item${if ((documentCounts[category.id] ?: 0) != 1) "s" else ""}",
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
                        count = "${documentCounts[category.id] ?: 0} item${if ((documentCounts[category.id] ?: 0) != 1) "s" else ""}",
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
                    fontSize = 54.sp
                )
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 22.dp)
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