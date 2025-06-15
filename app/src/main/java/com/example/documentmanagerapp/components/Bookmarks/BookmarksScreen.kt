package com.example.documentmanagerapp.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.repository.BookmarkRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookmarkViewModel(
    private val repository: BookmarkRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    var groupedBookmarks by mutableStateOf<List<BookmarkGroup>>(emptyList())
        private set
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun fetchBookmarks(userId: Long) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                Log.d("BookmarkViewModel", "Fetching bookmarks for userId: $userId")
                val bookmarks = repository.getBookmarksByUserId(userId)
                Log.d("BookmarkViewModel", "Fetched ${bookmarks.size} bookmarks")
                groupedBookmarks = groupBookmarksByDate(bookmarks)
            } catch (e: Exception) {
                error = "Không thể tải bookmark: ${e.message}"
                Log.e("BookmarkViewModel", "Error fetching bookmarks: ${e.message}")
            } finally {
                loading = false
            }
        }
    }

    fun toggleFavoriteBookmark(documentId: Long, userId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(documentId)
                fetchBookmarks(userId)
                onSuccess()
                Log.d("BookmarkViewModel", "Toggled favorite for document: $documentId")
            } catch (e: Exception) {
                onError("Không thể cập nhật trạng thái yêu thích: ${e.message}")
                Log.e("BookmarkViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }

    private fun groupBookmarksByDate(bookmarks: List<BookmarkData>): List<BookmarkGroup> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val grouped = mutableListOf(
            BookmarkGroup("Today", mutableListOf()),
            BookmarkGroup("Yesterday", mutableListOf()),
            BookmarkGroup("Older", mutableListOf())
        )

        val emojis = listOf("🌈", "😺", "🧠", "🛸", "✈️", "💼", "🎯")
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())

        bookmarks.forEachIndexed { index, bookmark ->
            val createdAt = bookmark.createdAt?.let { isoFormat.parse(it) } ?: Date()
            val isToday = createdAt >= today
            val isYesterday = createdAt >= yesterday && createdAt < today

            val categoryName = bookmark.document.category?.name ?: "Unsorted"
            val bookmarkItem = BookmarkItem(
                bookmark = bookmark,
                icon = if (bookmark.isFavorite) "heart" else "document-outline",
                title = bookmark.document.documentName,
                source = bookmark.document.encryptionMethod ?: "Unknown",
                category = categoryName,
                categoryColor = if (bookmark.document.category?.name != null) Color(0xFFFF69B4) else Color(0xFF1E90FF),
                emoji = emojis[index % emojis.size],
                time = timeFormat.format(createdAt)
            )

            when {
                isToday -> grouped[0].items.add(bookmarkItem)
                isYesterday -> grouped[1].items.add(bookmarkItem)
                else -> grouped[2].items.add(bookmarkItem)
            }
        }

        return grouped.filter { it.items.isNotEmpty() }
    }
}

data class BookmarkGroup(val text: String, val items: MutableList<BookmarkItem>)
data class BookmarkItem(
    val bookmark: BookmarkData,
    val icon: String,
    val title: String,
    val source: String,
    val category: String,
    val categoryColor: Color,
    val emoji: String,
    val time: String
)

class BookmarkViewModelFactory(
    private val repository: BookmarkRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarkViewModel::class.java)) {
            return BookmarkViewModel(repository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun BookmarkScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val repository = BookmarkRepository(context)
    val viewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModelFactory(repository, authViewModel)
    )
    val user by authViewModel.user.observeAsState()
    val groupedBookmarks by remember { derivedStateOf { viewModel.groupedBookmarks } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val error by remember { derivedStateOf { viewModel.error } }

    var dialogBookmark by remember { mutableStateOf<BookmarkItem?>(null) }
    var showToggleFavoriteDialog by remember { mutableStateOf<BookmarkItem?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(user?.id) {
        Log.d("BookmarkScreen", "User ID: ${user?.id}")
        user?.id?.let { userId ->
            viewModel.fetchBookmarks(userId)
            visible = true
        } ?: run {
            viewModel.error = "Vui lòng đăng nhập để xem bookmark!"
            viewModel.loading = false
            visible = true
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Dialog chi tiết bookmark
    dialogBookmark?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { dialogBookmark = null },
            title = { Text(bookmark.title) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            dialogBookmark = null
                            navController.navigate("FileDetails/${bookmark.bookmark.document.id}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "See details",
                            color = Color(0xFF1E3A8A),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TextButton(
                        onClick = {
                            dialogBookmark = null
                            showToggleFavoriteDialog = bookmark // Kích hoạt dialog toggle favorite
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = if (bookmark.bookmark.isFavorite) "Remove Favorite" else "Add Favorite",
                            color = Color.Red,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogBookmark = null }) {
                    Text("Cancel", color = Color(0xFF1E3A8A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog xác nhận toggle favorite
    showToggleFavoriteDialog?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { showToggleFavoriteDialog = null },
            title = { Text(if (bookmark.bookmark.isFavorite) "Xóa khỏi yêu thích" else "Thêm vào yêu thích") },
            text = { Text("Bạn có chắc chắn muốn ${if (bookmark.bookmark.isFavorite) "xóa" else "thêm"} ${bookmark.title} ${if (bookmark.bookmark.isFavorite) "khỏi" else "vào"} danh sách yêu thích?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        user?.id?.let { userId ->
                            viewModel.toggleFavoriteBookmark(
                                documentId = bookmark.bookmark.document.id,
                                userId = userId,
                                onSuccess = {
                                    Toast.makeText(context, if (bookmark.bookmark.isFavorite) "Đã xóa khỏi yêu thích!" else "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show()
                                    showToggleFavoriteDialog = null
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    showToggleFavoriteDialog = null
                                }
                            )
                        }
                    }
                ) {
                    Text("Xác nhận", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleFavoriteDialog = null }) {
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
            CircularProgressIndicator(color = Color(0xFF1E3A8A))
        }
        return
    }

    if (error != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error ?: "Lỗi không xác định",
                fontSize = 16.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Button(
                onClick = { user?.id?.let { userId -> viewModel.fetchBookmarks(userId) } },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Thử lại", color = Color.White, fontSize = 16.sp)
            }
        }
        return
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bookmarks",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFAAAAAA)
                        )
                    },
                    placeholder = { Text("Search your bookmark", color = Color(0xFFAAAAAA)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE6F0FA))
                        .clickable { navController.navigate("Search") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            if (groupedBookmarks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recent bookmarks",
                            fontSize = 16.sp,
                            color = Color(0xFF888888),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                groupedBookmarks.forEach { section ->
                    item {
                        Text(
                            text = section.text,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                        )
                    }
                    items(section.items) { item ->
                        RenderBookmarkItem(
                            item = item,
                            onEllipsisClick = { dialogBookmark = item },
                            onClick = { navController.navigate("FileDetails/${item.bookmark.document.id}") }
                        )
                        if (section.items.last() != item) {
                            Divider(
                                color = Color(0xFFD3D3D3),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderBookmarkItem(
    item: BookmarkItem,
    onEllipsisClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.icon == "heart") Icons.Filled.Favorite else Icons.Default.Description,
            contentDescription = item.icon,
            tint = Color(0xFF888888),
            modifier = Modifier
                .size(40.dp)
                .padding(end = 10.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = item.source,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Row {
                Text(
                    text = "${item.emoji} ${item.category} • ${item.time}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
        IconButton(onClick = onEllipsisClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
