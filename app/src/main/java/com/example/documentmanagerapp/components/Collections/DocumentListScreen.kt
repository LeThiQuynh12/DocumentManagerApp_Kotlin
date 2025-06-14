package com.example.documentmanagerapp.components.Collections

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.utils.data.Document
import com.example.documentmanagerapp.utils.repository.CollectionsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DocumentListViewModel(
    private val repository: CollectionsRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    var documents by mutableStateOf<List<Document>>(emptyList())
        private set
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var refreshing by mutableStateOf(false)
        private set

    fun fetchDocuments(userId: Long, categoryId: Long) {
        if (documents.isNotEmpty() && !refreshing) return
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val response = repository.getDocuments(userId)
                val filteredDocuments = response.filter { it.category?.id == categoryId }
                documents = filteredDocuments
            } catch (e: Exception) {
                error = if (e.message == "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.") {
                    e.message
                } else {
                    "Không thể tải danh sách tài liệu: ${e.message}"
                }
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    fun refreshDocuments(userId: Long, categoryId: Long) {
        refreshing = true
        fetchDocuments(userId, categoryId)
    }

    fun toggleFavorite(documentId: Long, isFavorite: Boolean, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val updatedDocument = repository.toggleFavorite(documentId)
                documents = documents.map { doc ->
                    if (doc.id == documentId) doc.copy(isFavorite = updatedDocument.isFavorite) else doc
                }
                onSuccess(updatedDocument.isFavorite)
            } catch (e: Exception) {
                error = if (e.message == "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.") {
                    e.message
                } else {
                    "Không thể cập nhật trạng thái yêu thích: ${e.message}"
                }
            }
        }
    }
}

class DocumentListViewModelFactory(
    private val repository: CollectionsRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentListViewModel::class.java)) {
            return DocumentListViewModel(repository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(navController: NavController, categoryId: Long?, categoryName: String?) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val repository = CollectionsRepository(context)
    val viewModel: DocumentListViewModel = viewModel(factory = DocumentListViewModelFactory(repository, authViewModel))
    val user by authViewModel.user.observeAsState()
    val documents by remember { derivedStateOf { viewModel.documents } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val error by remember { derivedStateOf { viewModel.error } }
    val refreshing by mutableStateOf(viewModel.refreshing)
    var showFavoriteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun safeNavigate(route: String) {
        try {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        } catch (e: Exception) {
            Log.e("DocumentListScreen", "Navigation error: ${e.message}")
            Toast.makeText(context, "Lỗi điều hướng: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = user?.id, key2 = categoryId) {
        user?.id?.let { userId ->
            categoryId?.let { catId ->
                viewModel.fetchDocuments(userId, catId)
            } ?: run {
                viewModel.error = "Không tìm thấy danh mục"
                viewModel.loading = false
            }
        } ?: run {
            viewModel.error = "Không tìm thấy người dùng"
            viewModel.loading = false
           // safeNavigate("login")
        }
    }

    LaunchedEffect(error) {
        error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (message == "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.") {
                coroutineScope.launch {
                    authViewModel.logout()
                   // safeNavigate("login")
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(
            onDismissRequest = { showFavoriteDialog = false },
            title = { Text("Thêm vào yêu thích thành công") },
            text = { Text("Tài liệu đã được thêm vào mục yêu thích!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        safeNavigate("bookmarks")
                        showFavoriteDialog = false
                    }
                ) {
                    Text("Xem Bookmarks")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFavoriteDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp)
                .shadow(5.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                } else {
                    safeNavigate("collections")
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E3A8A)
                )
            }
            Text(
                text = categoryName ?: "Danh mục",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.width(28.dp))
        }

        if (loading && !refreshing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Đang tải tài liệu...",
                    fontSize = 16.sp,
                    color = Color(0xFF1E3A8A)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (documents.isEmpty() && !loading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Empty",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Không có tài liệu trong danh mục này",
                                fontSize = 16.sp,
                                color = Color(0xFF6B7280),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(documents, key = { document -> document.id }) { document ->
                        DocumentItem(
                            document = document,
                            onViewDetails = {
                                try {
                                    if (user?.id == null) {
                                        throw Exception("Người dùng chưa đăng nhập")
                                    }
                                    safeNavigate("fileDetails/${document.id}")
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(document.id, document.isFavorite) { isFavorite ->
                                    val message = if (isFavorite) "Thêm vào yêu thích thành công" else "Xóa khỏi yêu thích"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (isFavorite) {
                                        showFavoriteDialog = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        error?.let { message ->
            if (message != "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F7FA))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color(0xFFEF4444),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Button(
                        onClick = {
                            user?.id?.let { userId ->
                                categoryId?.let { catId ->
                                    viewModel.fetchDocuments(userId, catId)
                                }
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFFF97316), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Thử lại",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    document: Document,
    onViewDetails: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(15.dp)
            .clickable { onViewDetails() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.fileType.lowercase()) {
                        "pdf" -> Icons.Default.Description
                        "doc", "docx" -> Icons.Default.Description
                        else -> Icons.Default.Description
                    },
                    contentDescription = "File Type",
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column {
                Text(
                    text = document.documentName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E3A8A),
                    maxLines = 1
                )
                Text(
                    text = "${document.fileType.uppercase()} • ${
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
                            val date = sdf.parse(document.createdAt)
                            SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(date)
                        } catch (e: Exception) {
                            "N/A"
                        }
                    }",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onViewDetails) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = "View Details",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (document.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (document.isFavorite) Color(0xFFFF0000) else Color(0xFF7C3AED),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}