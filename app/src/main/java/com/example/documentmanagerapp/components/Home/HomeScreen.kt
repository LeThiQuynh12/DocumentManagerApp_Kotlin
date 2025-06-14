package com.example.documentmanagerapp.components.Home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.documentmanagerapp.R
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.repository.BookmarkRepository
import com.example.documentmanagerapp.utils.repository.CollectionsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val collectionsRepository: CollectionsRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    var bookmarks by mutableStateOf<List<BookmarkData>>(emptyList())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var documentCounts by mutableStateOf<Map<Long, Int>>(emptyMap())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun fetchData(userId: Long) {
        viewModelScope.launch {
            loading = true
            try {
                // Run fetch operations in parallel
                val categoriesDeferred = async { collectionsRepository.fetchData(userId) }
                val bookmarksDeferred = async { bookmarkRepository.getBookmarksByUserId(userId) }

                // Await results
                val (fetchedCategories, fetchedCounts) = categoriesDeferred.await()
                val fetchedBookmarks = bookmarksDeferred.await()

                categories = fetchedCategories
                documentCounts = fetchedCounts
                bookmarks = fetchedBookmarks.sortedByDescending { it.createdAt }
                error = null
                Log.d("HomeViewModel", "Fetched Categories: $fetchedCategories, Bookmarks: $fetchedBookmarks")
            } catch (e: Exception) {
                error = e.message
                Log.e("HomeViewModel", "Error fetching data: ${e.message}")
            } finally {
                loading = false
            }
        }
    }
}

class HomeViewModelFactory(
    private val collectionsRepository: CollectionsRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(collectionsRepository, bookmarkRepository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val collectionsRepository = CollectionsRepository(context)
    val bookmarkRepository = BookmarkRepository(context)
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(collectionsRepository, bookmarkRepository, authViewModel)
    )
    val userState = authViewModel.user.observeAsState()
    val user = userState.value

    val bookmarks by remember { derivedStateOf { viewModel.bookmarks } }
    val categories by remember { derivedStateOf { viewModel.categories } }
    val documentCounts by remember { derivedStateOf { viewModel.documentCounts } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val error by remember { derivedStateOf { viewModel.error } }
    val emojis = listOf("🌈", "😺", "🧠", "🛸")

    LaunchedEffect(user?.id) {
        user?.id?.let { userId ->
            viewModel.fetchData(userId)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Hello, John Doe!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(top = 40.dp, bottom = 20.dp)
        )

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .padding(horizontal = 12.dp)
                .clickable { onSearchClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search your bookmark",
                color = Color(0xFF888888),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Categories
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryButton("Links", R.drawable.ic_link, Color(0xFFE6E6FA), Color(0xFF6A5ACD))
            CategoryButton("Images", R.drawable.ic_image, Color(0xFFE0F7FA), Color(0xFF00CED1))
            CategoryButton("Documents", R.drawable.ic_document, Color(0xFFFFE4E1), Color(0xFFFF6347))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // My Collections
        SectionHeader(
            title = "My Collections",
            seeAllText = "See All 〉",
            onSeeAllClick = { navController.navigate("collections") }
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
            }
        } else if (categories.isEmpty()) {
            Text(
                text = "No collections available",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val category = categories[index]
                    SimpleCategoryCard(
                        name = category.name,
                        count = documentCounts[category.id] ?: 0,
                        emoji = emojis[index % emojis.size],
                        onClick = {
                            navController.navigate("documentList/${category.id}/${category.name}")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Bookmark
        SectionHeader(
            title = "Recent Bookmark",
            seeAllText = "", // No "See All" for recent bookmarks
            onSeeAllClick = {}
        )

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
            }
        } else if (bookmarks.isEmpty()) {
            Text(
                text = "No recent bookmarks",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            // Display the most recent bookmark (or multiple, if desired)
            val recentBookmarks = bookmarks.take(1) // Show only the most recent bookmark
            recentBookmarks.forEach { bookmark ->
                BookmarkItem(
                    bookmark = bookmark,
                    onClick = {
                        bookmark.document.id?.let { id ->
                            navController.navigate("fileDetails/$id")
                        }
                    },
                    onMoreClick = {
                        showBookmarkDialog(context, bookmark, bookmarkRepository, viewModel) {
                            viewModel.fetchData(user?.id ?: return@showBookmarkDialog)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BookmarkItem(
    bookmark: BookmarkData,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                when (bookmark.document.fileType) {
                    "Link" -> R.drawable.ic_link
                    "Image" -> R.drawable.ic_image
                    else -> R.drawable.ic_document
                }
            ),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bookmark.document.documentName,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = bookmark.document.category?.name ?: "Unknown Category",
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_palette),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF69B4)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${bookmark.document.category?.name ?: "Unsorted"} • ${
                        try {
                            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            bookmark.createdAt?.let { createdAt ->
                                isoFormat.parse(createdAt)?.let { timeFormat.format(it) } ?: "N/A"
                            } ?: "N/A"
                        } catch (e: Exception) {
                            Log.e("BookmarkItem", "Error parsing date: ${bookmark.createdAt}, ${e.message}")
                            "N/A"
                        }
                    }",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun CategoryButton(
    label: String,
    iconId: Int,
    bgColor: Color,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .background(bgColor, shape = MaterialTheme.shapes.medium)
            .padding(15.dp)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun SimpleCategoryCard(
    name: String,
    count: Int,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .width(170.dp)
            .height(120.dp)
            .clickable { onClick() }
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Text(
                text = "$count item${if (count != 1) "s" else ""}",
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    seeAllText: String,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )
        if (seeAllText.isNotEmpty()) {
            Text(
                text = seeAllText,
                fontSize = 14.sp,
                color = Color(0xFF1E90FF),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

private fun showBookmarkDialog(
    context: android.content.Context,
    bookmark: BookmarkData,
    repository: BookmarkRepository,
    viewModel: HomeViewModel,
    onBookmarkToggled: () -> Unit
) {
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle(bookmark.document.documentName)
        .setItems(arrayOf("Edit Bookmark", "Toggle Favorite")) { _, which ->
            when (which) {
                0 -> {
                    // Navigate to edit bookmark screen if implemented
                    // navController.navigate("editBookmark/${bookmark.id}")
                }
                1 -> {
                    viewModel.viewModelScope.launch {
                        try {
                            bookmark.document.id?.let { documentId ->
                                repository.toggleFavorite(documentId)
                                onBookmarkToggled()
                                Toast.makeText(
                                    context,
                                    if (bookmark.isFavorite) "Đã xóa khỏi yêu thích" else "Đã thêm vào yêu thích",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        .setNegativeButton("Hủy", null)
        .show()
}
