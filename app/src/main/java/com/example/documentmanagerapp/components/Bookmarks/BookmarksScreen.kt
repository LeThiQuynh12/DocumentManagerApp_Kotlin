package com.example.documentmanagerapp.components.Bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Data class representing a bookmark item
data class BookmarkItem(
    val title: String,
    val emoji: String,
    val time: String,
    val isFavorite: Boolean
)

// Composable function to render the Bookmarks screen
@Composable
fun BookmarksScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E3A8A)
                    )
                }
                Text(
                    text = "Bookmarks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = "",
                onValueChange = { /* Handle search */ },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF888888)
                    )
                },
                placeholder = { Text("Search your bookmark", color = Color(0xFF888888)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE6F0FA)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E90FF),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF1E90FF)
                ),
                singleLine = true
            )
        }

        // Today Section
        item {
            Text(
                text = "Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            RenderBookmarkItem(
                BookmarkItem("Huhuhu", "🌈", "AES - Triền - 15:11", true)
            )
        }

        // Older Section
        item {
            Text(
                text = "Older",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }
        items(
            listOf(
                BookmarkItem("IMG_8530.png", "🌈", "AES - Triền - 13:56", true),
                BookmarkItem("IMG_8574.png", "😺", "AES - Thứ 5 - 22:56", true),
                BookmarkItem("Unknown%202.jpeg", "😺", "AES - Thứ 5 - 22:53", true),
                BookmarkItem("TriTueNhanTaoTienViet.docx", "🤓", "AES - Thứ 5 - 22:13", true),
                BookmarkItem("IMG_8530.png", "🌈", "AES - Triền - 22:54", true),
                BookmarkItem("RaiThuHoach.docx", "🌱", "AES - Triền - 22:54", false)
            )
        ) { item ->
            RenderBookmarkItem(item)
        }
    }
}

// Composable function to render a single bookmark item
@Composable
fun RenderBookmarkItem(item: BookmarkItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorite",
            tint = if (item.isFavorite) Color(0xFFFF4500) else Color(0xFF888888),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = "${item.emoji}  ${item.time}",
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
        }
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = Color(0xFF888888),
            modifier = Modifier.size(20.dp)
        )
    }
}