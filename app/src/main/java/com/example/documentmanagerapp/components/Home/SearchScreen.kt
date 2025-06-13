//
//package com.example.documentmanagerapp.components
//
//import android.content.Context
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.focusable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.example.documentmanagerapp.utils.repository.DocumentRepository
//import com.example.documentmanagerapp.utils.data.DocumentData
//import kotlinx.coroutines.launch
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.text.style.TextAlign
//import com.example.documentmanagerapp.context.AuthViewModel
//import com.example.documentmanagerapp.components.context.AuthViewModelFactory
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val coroutineScope = rememberCoroutineScope()
//    val documentRepository = DocumentRepository(context)
//    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
//    val userState = authViewModel.user.observeAsState()
//    val user = userState.value
//
//    var searchQuery by rememberSaveable { mutableStateOf("") }
//    var isSearchFocused by remember { mutableStateOf(false) }
//    var recentSearches by rememberSaveable { mutableStateOf(listOf<String>()) }
//    var selectedTab by rememberSaveable { mutableStateOf<String?>(null) }
//    var documents by remember { mutableStateOf(listOf<DocumentData>()) }
//    var isSearching by remember { mutableStateOf(false) }
//    val searchSuggestions = listOf("Inspiration", "UI/UX Design", "Web Development", "Thảo", "Technology")
//    var currentTab by rememberSaveable { mutableStateOf("initial") } // initial, searchFocused, searchResults
//
//    // Load recent searches from SharedPreferences
//    LaunchedEffect(Unit) {
//        recentSearches = loadRecentSearches(context)
//    }
//
//    // Tabs data
//    data class TabItem(val type: String, val name: String, val icon: ImageVector, val color: Color)
//    val tabs = listOf(
//        TabItem("favorites", "Favorites", Icons.Default.Favorite, Color(0xFFFF4500)),
//        TabItem("url", "Links", Icons.Default.Link, Color(0xFF6A5ACD)),
//        TabItem("png", "Images", Icons.Default.Image, Color(0xFF00CED1)),
//        TabItem("mp4", "Video", Icons.Default.Videocam, Color(0xFFFFD700)),
//        TabItem("document", "Documents", Icons.Default.Description, Color(0xFFFF6347))
//    )
//
//    // Filter suggestions based on selected tab
//    fun getTabSuggestions(): List<String> {
//        if (selectedTab == null) return searchSuggestions
//        return when (selectedTab) {
//            "favorites" -> listOf("Favorite Design", "Top Picks", "Best UI")
//            "url" -> listOf("Useful Links", "Resources", "Tutorials")
//            "png" -> listOf("Design Images", "Photos", "Graphics")
//            "mp4" -> listOf("Tutorials Video", "How-to Clips", "Vlogs")
//            "document" -> listOf("PDF Guides", "Reports", "Docs")
//            else -> searchSuggestions
//        }
//    }
//
//    // Tìm kiếm theo loại tài liệu (dùng cho tabs)
//    fun searchDocumentsByFileType(fileType: String, onResult: (List<DocumentData>) -> Unit) {
//        if (user == null) {
//            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
//            return
//        }
//        coroutineScope.launch {
//            try {
//                isSearching = true
//                val results = if (fileType == "favorites") {
//                    documentRepository.getFavoriteDocuments(user.id.toLong())
//                } else {
//                    documentRepository.searchDocumentsByFileType(fileType)
//                }
//                onResult(results)
//            } catch (e: Exception) {
//                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
//                onResult(emptyList())
//            } finally {
//                isSearching = false
//            }
//        }
//    }
//
//    // Tìm kiếm theo từ khóa (dùng cho thanh tìm kiếm)
//    fun searchDocumentsByKeyword(keyword: String, onResult: (List<DocumentData>) -> Unit) {
//        if (user == null) {
//            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
//            return
//        }
//        coroutineScope.launch {
//            try {
//                isSearching = true
//                val results = documentRepository.searchDocumentsByKeyword(keyword)
//                onResult(results)
//            } catch (e: Exception) {
//                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
//                onResult(emptyList())
//            } finally {
//                isSearching = false
//            }
//        }
//    }
//
//    // Handle search
//    fun handleSearch(query: String = searchQuery) {
//        val keyword = query.trim()
//        if (keyword.isEmpty() && selectedTab == null) return
//
//        if (keyword.isNotEmpty()) {
//            searchDocumentsByKeyword(keyword) { results ->
//                documents = results
//                currentTab = "searchResults"
//                val updatedSearches = (listOf(keyword) + recentSearches.filter { it.lowercase() != keyword.lowercase() }).take(8)
//                recentSearches = updatedSearches
//                saveRecentSearches(context, updatedSearches)
//                keyboardController?.hide()
//            }
//        } else {
//            selectedTab?.let { tabType ->
//                searchDocumentsByFileType(tabType) { results ->
//                    documents = results
//                    currentTab = "searchResults"
//                    keyboardController?.hide()
//                }
//            }
//        }
//    }
//
//    // Handle tab selection
//    fun handleTabSelect(tabType: String, tabName: String) {
//        selectedTab = tabType
//        searchQuery = tabName
//        currentTab = "searchResults"
//        searchDocumentsByFileType(tabType) { results ->
//            documents = results
//            val updatedSearches = (listOf(tabName) + recentSearches.filter { it.lowercase() != tabName.lowercase() }).take(8)
//            recentSearches = updatedSearches
//            saveRecentSearches(context, updatedSearches)
//        }
//    }
//
//    // Handle clear search
//    fun handleClearSearch() {
//        searchQuery = ""
//        selectedTab = null
//        documents = emptyList()
//        currentTab = "initial"
//        isSearchFocused = false
//        keyboardController?.hide()
//    }
//
//    // Remove recent search
//    fun removeRecentSearch(index: Int) {
//        recentSearches = recentSearches.toMutableList().apply { removeAt(index) }
//        saveRecentSearches(context, recentSearches)
//    }
//
//    // Clear all recent searches
//    fun clearRecentSearches() {
//        recentSearches = emptyList()
//        saveRecentSearches(context, emptyList())
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F7FA))
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 40.dp)
//        ) {
//            // Search Bar
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                if (isSearchFocused) {
//                    IconButton(onClick = { handleClearSearch() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Clear", tint = Color(0xFF1E3A8A))
//                    }
//                } else {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A8A))
//                    }
//                }
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(50.dp)
//                        .clip(RoundedCornerShape(25.dp))
//                        .shadow(2.dp, RoundedCornerShape(25.dp))
//                        .background(Color.White)
//                        .focusable()
//                        .onFocusChanged { isSearchFocused = it.isFocused },
//                    leadingIcon = {
//                        Icon(
//                            Icons.Default.Search,
//                            contentDescription = "Search",
//                            tint = if (isSearchFocused) Color(0xFF1E90FF) else Color(0xFF888888)
//                        )
//                    },
//                    trailingIcon = {
//                        if (searchQuery.isNotEmpty()) {
//                            IconButton(onClick = { handleClearSearch() }) {
//                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF888888))
//                            }
//                        }
//                    },
//                    placeholder = {
//                        Text("Search your documents...", color = Color(0xFF888888))
//                    },
//                    colors = TextFieldDefaults.outlinedTextFieldColors(
//                        focusedBorderColor = Color(0xFF1E90FF),
//                        unfocusedBorderColor = Color.Transparent,
//                        containerColor = Color.White
//                    ),
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
//                    keyboardActions = KeyboardActions(onSearch = { handleSearch() })
//                )
//            }
//
//            when {
//                isSearching -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            CircularProgressIndicator(color = Color(0xFF1E90FF))
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text("Đang tìm kiếm...", color = Color(0xFF333333), fontSize = 16.sp)
//                        }
//                    }
//                }
//                documents.isNotEmpty() -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 16.dp)
//                    ) {
//                        item {
//                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
//                                Text(
//                                    text = "Kết quả cho \"${searchQuery.ifEmpty { selectedTab ?: "" }}\"",
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF333333)
//                                )
//                                Text(
//                                    text = "${documents.size} kết quả phù hợp",
//                                    fontSize = 14.sp,
//                                    color = Color(0xFF888888),
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//                        }
//                        items(documents, key = { it.id }) { document ->
//                            DocumentItem(
//                                document = document,
//                                onClick = {
//                                    navController.navigate("FileDetails/${document.id}/Bookmarks")
//                                }
//                            )
//                        }
//                    }
//                }
//                searchQuery.trim().isNotEmpty() -> {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(20.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = "Không tìm thấy kết quả",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333),
//                            textAlign = TextAlign.Center
//                        )
//                        Text(
//                            text = "Không có tài liệu nào phù hợp với \"$searchQuery\"",
//                            fontSize = 16.sp,
//                            color = Color(0xFF888888),
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.padding(top = 10.dp, bottom = 30.dp)
//                        )
//                        Text(
//                            text = "Bạn có thể thử:",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//                        Row(
//                            modifier = Modifier.padding(10.dp),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            searchSuggestions.take(3).forEach { suggestion ->
//                                SuggestionTag(
//                                    text = suggestion,
//                                    onClick = {
//                                        searchQuery = suggestion
//                                        handleSearch(suggestion)
//                                    }
//                                )
//                            }
//                        }
//                        Button(
//                            onClick = { handleClearSearch() },
//                            modifier = Modifier
//                                .padding(top = 20.dp)
//                                .height(48.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
//                        ) {
//                            Text("Thử từ khóa khác", color = Color.White, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                }
//                isSearchFocused -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 16.dp)
//                    ) {
//                        if (recentSearches.isNotEmpty()) {
//                            item {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 12.dp),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Tìm kiếm gần đây",
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF1E3A8A)
//                                    )
//                                    Text(
//                                        text = "Xóa tất cả",
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF1E90FF),
//                                        modifier = Modifier.clickable { clearRecentSearches() }
//                                    )
//                                }
//                            }
//                            items(recentSearches, key = { it }) { search ->
//                                RecentSearchItem(
//                                    text = search,
//                                    onClick = {
//                                        searchQuery = search
//                                        isSearchFocused = false
//                                        handleSearch(search)
//                                    },
//                                    onClear = { removeRecentSearch(recentSearches.indexOf(search)) }
//                                )
//                            }
//                        }
//                        item {
//                            Text(
//                                text = "Gợi ý tìm kiếm",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF1E3A8A),
//                                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
//                            )
//                        }
//                        items(getTabSuggestions(), key = { it }) { suggestion ->
//                            SuggestionItem(
//                                text = suggestion,
//                                onClick = {
//                                    searchQuery = suggestion
//                                    isSearchFocused = false
//                                    handleSearch(suggestion)
//                                }
//                            )
//                        }
//                    }
//                }
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 16.dp)
//                    ) {
//                        item {
//                            Column(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
//                                tabs.forEach { tab ->
//                                    TabItem(
//                                        icon = tab.icon,
//                                        text = tab.name,
//                                        color = tab.color,
//                                        isSelected = selectedTab == tab.type,
//                                        onClick = { handleTabSelect(tab.type, tab.name) }
//                                    )
//                                }
//                            }
//                        }
//                        if (recentSearches.isNotEmpty()) {
//                            item {
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 12.dp),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Tìm kiếm gần đây",
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF1E3A8A)
//                                    )
//                                    Text(
//                                        text = "Xóa tất cả",
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF1E90FF),
//                                        modifier = Modifier.clickable { clearRecentSearches() }
//                                    )
//                                }
//                            }
//                            items(recentSearches, key = { it }) { search ->
//                                RecentSearchItem(
//                                    text = search,
//                                    onClick = {
//                                        searchQuery = search
//                                        isSearchFocused = false
//                                        handleSearch(search)
//                                    },
//                                    onClear = { removeRecentSearch(recentSearches.indexOf(search)) }
//                                )
//                            }
//                        }
//                        item {
//                            Text(
//                                text = "Gợi ý tìm kiếm",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF1E3A8A),
//                                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
//                            )
//                        }
//                        items(getTabSuggestions(), key = { it }) { suggestion ->
//                            SuggestionItem(
//                                text = suggestion,
//                                onClick = {
//                                    searchQuery = suggestion
//                                    isSearchFocused = false
//                                    handleSearch(suggestion)
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TabItem(icon: ImageVector, text: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .background(if (isSelected) Color(0x1A5271FF) else Color.White)
//            .clickable(onClick = onClick)
//            .padding(horizontal = 20.dp, vertical = 13.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
//        Spacer(modifier = Modifier.width(10.dp))
//        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
//    }
//}
//
//@Composable
//fun RecentSearchItem(text: String, onClick: () -> Unit, onClear: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp)
//            .clickable(onClick = onClick),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
//            Spacer(modifier = Modifier.width(12.dp))
//            Text(text = text, fontSize = 16.sp, color = Color(0xFF333333))
//        }
//        IconButton(onClick = onClear) {
//            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
//        }
//    }
//}
//
//@Composable
//fun SuggestionItem(text: String, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp)
//            .clickable(onClick = onClick),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
//        Spacer(modifier = Modifier.width(12.dp))
//        Text(text = text, fontSize = 16.sp, color = Color(0xFF333333))
//    }
//}
//
//@Composable
//fun DocumentItem(document: DocumentData, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 12.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEFF)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Column(modifier = Modifier.padding(12.dp)) {
//            Text(
//                text = document.documentName ?: "Untitled",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF333333),
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(12.dp))
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = "${document.fileType} - ${document.category?.name ?: "No Category"}",
//                    fontSize = 13.sp,
//                    color = Color(0xFF888888)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun SuggestionTag(text: String, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .clip(RoundedCornerShape(20.dp))
//            .clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA))
//    ) {
//        Text(
//            text = text,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color(0xFF1E90FF),
//            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
//        )
//    }
//}
//
//private fun saveRecentSearches(context: Context, searches: List<String>) {
//    context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
//        .edit()
//        .putString("recent_searches", searches.joinToString(","))
//        .apply()
//}
//
//private fun loadRecentSearches(context: Context): List<String> {
//    val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
//    return prefs.getString("recent_searches", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
//}




package com.example.documentmanagerapp.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.documentmanagerapp.utils.repository.DocumentRepository
import com.example.documentmanagerapp.utils.data.DocumentData
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val documentRepository = DocumentRepository(context)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val userState = authViewModel.user.observeAsState()
    val user = userState.value

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var recentSearches by rememberSaveable { mutableStateOf(listOf<String>()) }
    var selectedTab by rememberSaveable { mutableStateOf<String?>(null) }
    var documents by remember { mutableStateOf(listOf<DocumentData>()) }
    var isSearching by remember { mutableStateOf(false) }
    val searchSuggestions = listOf("Inspiration", "UI/UX Design", "Web Development", "Thảo", "Technology")
    var currentTab by rememberSaveable { mutableStateOf("initial") } // initial, searchFocused, searchResults
    var isSearchTriggered by rememberSaveable { mutableStateOf(false) } // Thêm trạng thái để kiểm soát tìm kiếm

    // Load recent searches from SharedPreferences
    LaunchedEffect(Unit) {
        recentSearches = loadRecentSearches(context)
    }

    // Tabs data
    data class TabItem(val type: String, val name: String, val icon: ImageVector, val color: Color)
    val tabs = listOf(
        TabItem("favorites", "Favorites", Icons.Default.Favorite, Color(0xFFFF4500)),
        TabItem("url", "Links", Icons.Default.Link, Color(0xFF6A5ACD)),
        TabItem("png", "Images", Icons.Default.Image, Color(0xFF00CED1)),
        TabItem("mp4", "Video", Icons.Default.Videocam, Color(0xFFFFD700)),
        TabItem("document", "Documents", Icons.Default.Description, Color(0xFFFF6347))
    )

    // Filter suggestions based on selected tab
    fun getTabSuggestions(): List<String> {
        if (selectedTab == null) return searchSuggestions
        return when (selectedTab) {
            "favorites" -> listOf("Favorite Design", "Top Picks", "Best UI")
            "url" -> listOf("Useful Links", "Resources", "Tutorials")
            "png" -> listOf("Design Images", "Photos", "Graphics")
            "mp4" -> listOf("Tutorials Video", "How-to Clips", "Vlogs")
            "document" -> listOf("PDF Guides", "Reports", "Docs")
            else -> searchSuggestions
        }
    }

    // Tìm kiếm theo loại tài liệu (dùng cho tabs)
    fun searchDocumentsByFileType(fileType: String, onResult: (List<DocumentData>) -> Unit) {
        if (user == null) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            try {
                isSearching = true
                val results = if (fileType == "favorites") {
                    documentRepository.getFavoriteDocuments(user.id.toLong())
                } else {
                    documentRepository.searchDocumentsByFileType(fileType)
                }
                onResult(results)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(emptyList())
            } finally {
                isSearching = false
            }
        }
    }

    // Tìm kiếm theo từ khóa (dùng cho thanh tìm kiếm)
    fun searchDocumentsByKeyword(keyword: String, onResult: (List<DocumentData>) -> Unit) {
        if (user == null) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            try {
                isSearching = true
                val results = documentRepository.searchDocumentsByKeyword(keyword)
                onResult(results)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(emptyList())
            } finally {
                isSearching = false
            }
        }
    }

    // Handle search
    fun handleSearch(query: String = searchQuery) {
        val keyword = query.trim()
        if (keyword.isEmpty() && selectedTab == null) return

        isSearchTriggered = true // Đặt trạng thái tìm kiếm khi gọi handleSearch
        if (keyword.isNotEmpty()) {
            searchDocumentsByKeyword(keyword) { results ->
                documents = results
                currentTab = "searchResults"
                val updatedSearches = (listOf(keyword) + recentSearches.filter { it.lowercase() != keyword.lowercase() }).take(8)
                recentSearches = updatedSearches
                saveRecentSearches(context, updatedSearches)
                keyboardController?.hide()
            }
        } else {
            selectedTab?.let { tabType ->
                searchDocumentsByFileType(tabType) { results ->
                    documents = results
                    currentTab = "searchResults"
                    keyboardController?.hide()
                }
            }
        }
    }

    // Handle tab selection
    fun handleTabSelect(tabType: String, tabName: String) {
        selectedTab = tabType
        searchQuery = tabName
        currentTab = "searchResults"
        isSearchTriggered = true // Đặt trạng thái tìm kiếm khi chọn tab
        searchDocumentsByFileType(tabType) { results ->
            documents = results
            val updatedSearches = (listOf(tabName) + recentSearches.filter { it.lowercase() != tabName.lowercase() }).take(8)
            recentSearches = updatedSearches
            saveRecentSearches(context, updatedSearches)
        }
    }

    // Handle clear search
    fun handleClearSearch() {
        searchQuery = ""
        selectedTab = null
        documents = emptyList()
        currentTab = "initial"
        isSearchTriggered = false // Đặt lại trạng thái tìm kiếm khi xóa
        isSearchFocused = false
        keyboardController?.hide()
    }

    // Remove recent search
    fun removeRecentSearch(index: Int) {
        recentSearches = recentSearches.toMutableList().apply { removeAt(index) }
        saveRecentSearches(context, recentSearches)
    }

    // Clear all recent searches
    fun clearRecentSearches() {
        recentSearches = emptyList()
        saveRecentSearches(context, emptyList())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchFocused) {
                    IconButton(onClick = { handleClearSearch() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Clear", tint = Color(0xFF1E3A8A))
                    }
                } else {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A8A))
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }, // Chỉ cập nhật query, không gọi handleSearch
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .shadow(2.dp, RoundedCornerShape(25.dp))
                        .background(Color.White)
                        .focusable()
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchFocused) Color(0xFF1E90FF) else Color(0xFF888888)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; handleClearSearch() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF888888))
                            }
                        }
                    },
                    placeholder = {
                        Text("Search your documents...", color = Color(0xFF888888))
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF1E90FF),
                        unfocusedBorderColor = Color.Transparent,
                        containerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        handleSearch() // Gọi handleSearch khi nhấn nút tìm kiếm
                        keyboardController?.hide()
                    })
                )
            }

            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1E90FF))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Đang tìm kiếm...", color = Color(0xFF333333), fontSize = 16.sp)
                        }
                    }
                }
                isSearchTriggered && documents.isNotEmpty() -> { // Chỉ hiển thị kết quả khi tìm kiếm đã được kích hoạt
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                Text(
                                    text = "Kết quả cho \"${searchQuery.ifEmpty { selectedTab ?: "" }}\"",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "${documents.size} kết quả phù hợp",
                                    fontSize = 14.sp,
                                    color = Color(0xFF888888),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        items(documents, key = { it.id }) { document ->
                            DocumentItem(
                                document = document,
                                onClick = {
                                    navController.navigate("FileDetails/${document.id}/Bookmarks")
                                }
                            )
                        }
                    }
                }
                isSearchTriggered && searchQuery.trim().isNotEmpty() && documents.isEmpty() -> { // Hiển thị "Không tìm thấy" khi tìm kiếm không có kết quả
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Không tìm thấy kết quả",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Không có tài liệu nào phù hợp với \"$searchQuery\"",
                            fontSize = 16.sp,
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp, bottom = 30.dp)
                        )
                        Text(
                            text = "Bạn có thể thử:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            searchSuggestions.take(3).forEach { suggestion ->
                                SuggestionTag(
                                    text = suggestion,
                                    onClick = {
                                        searchQuery = suggestion
                                        handleSearch(suggestion)
                                    }
                                )
                            }
                        }
                        Button(
                            onClick = { handleClearSearch() },
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                        ) {
                            Text("Thử từ khóa khác", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                isSearchFocused -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tìm kiếm gần đây",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = "Xóa tất cả",
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E90FF),
                                        modifier = Modifier.clickable { clearRecentSearches() }
                                    )
                                }
                            }
                            items(recentSearches, key = { it }) { search ->
                                RecentSearchItem(
                                    text = search,
                                    onClick = {
                                        searchQuery = search
                                        handleSearch(search)
                                        isSearchFocused = false
                                    },
                                    onClear = { removeRecentSearch(recentSearches.indexOf(search)) }
                                )
                            }
                        }
                        item {
                            Text(
                                text = "Gợi ý tìm kiếm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                            )
                        }
                        items(getTabSuggestions(), key = { it }) { suggestion ->
                            SuggestionItem(
                                text = suggestion,
                                onClick = {
                                    searchQuery = suggestion
                                    handleSearch(suggestion)
                                    isSearchFocused = false
                                }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                                tabs.forEach { tab ->
                                    TabItem(
                                        icon = tab.icon,
                                        text = tab.name,
                                        color = tab.color,
                                        isSelected = selectedTab == tab.type,
                                        onClick = { handleTabSelect(tab.type, tab.name) }
                                    )
                                }
                            }
                        }
                        if (recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tìm kiếm gần đây",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = "Xóa tất cả",
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E90FF),
                                        modifier = Modifier.clickable { clearRecentSearches() }
                                    )
                                }
                            }
                            items(recentSearches, key = { it }) { search ->
                                RecentSearchItem(
                                    text = search,
                                    onClick = {
                                        searchQuery = search
                                        handleSearch(search)
                                        isSearchFocused = false
                                    },
                                    onClear = { removeRecentSearch(recentSearches.indexOf(search)) }
                                )
                            }
                        }
                        item {
                            Text(
                                text = "Gợi ý tìm kiếm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                            )
                        }
                        items(getTabSuggestions(), key = { it }) { suggestion ->
                            SuggestionItem(
                                text = suggestion,
                                onClick = {
                                    searchQuery = suggestion
                                    handleSearch(suggestion)
                                    isSearchFocused = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(icon: ImageVector, text: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0x1A5271FF) else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
    }
}

@Composable
fun RecentSearchItem(text: String, onClick: () -> Unit, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 16.sp, color = Color(0xFF333333))
        }
        IconButton(onClick = onClear) {
            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SuggestionItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 16.sp, color = Color(0xFF333333))
    }
}

@Composable
fun DocumentItem(document: DocumentData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = document.documentName ?: "Untitled",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${document.fileType} - ${document.category?.name ?: "No Category"}",
                    fontSize = 13.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
fun SuggestionTag(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F0FA))
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E90FF),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        )
    }
}

private fun saveRecentSearches(context: Context, searches: List<String>) {
    context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        .edit()
        .putString("recent_searches", searches.joinToString(","))
        .apply()
}

private fun loadRecentSearches(context: Context): List<String> {
    val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    return prefs.getString("recent_searches", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
}