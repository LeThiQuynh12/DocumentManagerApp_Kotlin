
package com.example.documentmanagerapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // Added this import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 16.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF1E3A8A)
                    )
                },
                placeholder = {
                    Text("Search your documents...", color = Color(0xFF888888))
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1E3A8A),
                    unfocusedBorderColor = Color(0xFFD6D6D6)
                )
            )
        }

        // Filter Categories
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterItem(
                    icon = Icons.Default.Favorite,
                    text = "Favorites",
                    color = Color(0xFFE53E3E)
                )
                FilterItem(
                    icon = Icons.Default.Link,
                    text = "Links",
                    color = Color(0xFF9B59B6)
                )
                FilterItem(
                    icon = Icons.Default.Image,
                    text = "Images",
                    color = Color(0xFF2ECC71)
                )
                FilterItem(
                    icon = Icons.Default.Videocam,
                    text = "Video",
                    color = Color(0xFFF4A261)
                )
                FilterItem(
                    icon = Icons.Default.Description,
                    text = "Documents",
                    color = Color(0xFF3498DB)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recent Searches
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tim kiếm gần đây",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = "Xóa tất cả",
                    fontSize = 14.sp,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.clickable { /* TODO: Implement clear all logic */ }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RecentSearchItem("Documents", expandedCategories, { expandedCategories = it }) { /* TODO: Implement clear item logic */ }
                RecentSearchItem("Video", expandedCategories, { expandedCategories = it }) { /* TODO: Implement clear item logic */ }
                RecentSearchItem("Images", expandedCategories, { expandedCategories = it }) { /* TODO: Implement clear item logic */ }
                RecentSearchItem("Links", expandedCategories, { expandedCategories = it }) { /* TODO: Implement clear item logic */ }
                RecentSearchItem("Favorites", expandedCategories, { expandedCategories = it }) { /* TODO: Implement clear item logic */ }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search Suggestions
        item {
            Text(
                text = "Gợi ý tìm kiếm",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF1E3A8A)
                    )
                },
                placeholder = {
                    Text("Inspiration...", color = Color(0xFF888888))
                },
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1E3A8A),
                    unfocusedBorderColor = Color(0xFFD6D6D6)
                )
            )
        }
    }
}

@Composable
fun FilterItem(icon: ImageVector, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun RecentSearchItem(
    text: String,
    expandedCategories: Set<String>,
    onExpandChange: (Set<String>) -> Unit,
    onClear: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(expandedCategories.contains(text)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                isExpanded = !isExpanded
                onExpandChange(expandedCategories.toMutableSet().apply {
                    if (isExpanded) add(text) else remove(text)
                })
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isExpanded,
                    onClick = {
                        isExpanded = !isExpanded
                        onExpandChange(expandedCategories.toMutableSet().apply {
                            if (isExpanded) add(text) else remove(text)
                        })
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF1E3A8A),
                        unselectedColor = Color(0xFF888888)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = Color(0xFF888888),
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No recent $text found",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }
}