//package com.example.documentmanagerapp.components.Collections
//
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.documentmanagerapp.R
//import androidx.compose.ui.draw.rotate
//@Composable
//fun CollectionsScreen() {
//    var expandedMainBooster by remember { mutableStateOf(true) }
//    var expandedAnotherList by remember { mutableStateOf(false) }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F7FA))
//            .padding(16.dp)
//    ) {
//        item {
//            Text(
//                text = "Collections",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1E3A8A),
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//        }
//
//        item {
//            OutlinedTextField(
//                value = "",
//                onValueChange = {},
//                placeholder = { Text("Search your bookmark") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF1E90FF),
//                    unfocusedBorderColor = Color(0xFF888888),
//                    cursorColor = Color(0xFF1E90FF)
//                ),
//                leadingIcon = {
//                    Icon(
//                        painter = painterResource(id = android.R.drawable.ic_menu_search),
//                        contentDescription = "Search Icon",
//                        tint = Color(0xFF888888)
//                    )
//                }
//            )
//        }
//
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//        item {
//            ExpandableSection(
//                title = "Main Booster",
//                expanded = expandedMainBooster,
//                onClick = { expandedMainBooster = !expandedMainBooster }
//            ) {
//                CollectionItem("Triệu", 12, R.drawable.ic_folder)
//                CollectionItem("Thủy", 1, R.drawable.ic_folder)
//            }
//        }
//
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//        item {
//            ExpandableSection(
//                title = "Another Saved List",
//                expanded = expandedAnotherList,
//                onClick = { expandedAnotherList = !expandedAnotherList }
//            ) {
//                Text(
//                    text = "Không có danh mục trong Another Saved List.",
//                    color = Color(0xFF888888),
//                    fontSize = 14.sp,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//        }
//
//        item {
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}
//
//@Composable
//fun ExpandableSection(
//    title: String,
//    expanded: Boolean,
//    onClick: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .background(Color.White, shape = MaterialTheme.shapes.medium)
//            .padding(12.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = title,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF1E3A8A)
//            )
//            Icon(
//                imageVector = Icons.Default.ArrowDropDown,
//                contentDescription = "Expand Icon",
//                tint = Color(0xFF1E90FF),
//                modifier = Modifier.rotate(if (expanded) 180f else 0f)
//            )
//        }
//        if (expanded) {
//            content()
//        }
//    }
//}
//
//@Composable
//fun CollectionItem(name: String, count: Int, iconId: Int) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            painter = painterResource(id = iconId),
//            contentDescription = null,
//            modifier = Modifier.size(40.dp),
//            tint = Color.Unspecified
//        )
//        Spacer(modifier = Modifier.width(16.dp))
//        Column {
//            Text(
//                text = name,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color(0xFF333333)
//            )
//            Text(
//                text = "$count items",
//                fontSize = 14.sp,
//                color = Color(0xFF888888)
//            )
//        }
//    }
//}
package com.example.documentmanagerapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen() {
    var isMainBoosterExpanded by remember { mutableStateOf(true) }
    var isAnotherSavedListExpanded by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    val mainBoosterData = listOf(
        CollectionItemData("Triệu", 12, "🌈"),
        CollectionItemData("Thủy", 1, "😺")
    )
    val anotherSavedListData = emptyList<CollectionItemData>()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A)) },
                actions = {
                    IconButton(onClick = { /* Handle add */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF1E3A8A))
                    }
                    IconButton(onClick = { /* Handle sort */ }) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Sort", tint = Color(0xFF1E3A8A))
                    }
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1E3A8A))
                    }
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewModule else Icons.Default.ViewAgenda,
                            contentDescription = "Toggle View",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchBar()
            }

            item {
                ExpandableSection(
                    title = "Main Booster",
                    expanded = isMainBoosterExpanded,
                    onClick = { isMainBoosterExpanded = !isMainBoosterExpanded }
                ) {
                    CollectionsList(data = mainBoosterData, isGridView = isGridView)
                }
            }

            item {
                ExpandableSection(
                    title = "Another Saved List",
                    expanded = isAnotherSavedListExpanded,
                    onClick = { isAnotherSavedListExpanded = !isAnotherSavedListExpanded }
                ) {
                    if (anotherSavedListData.isEmpty()) {
                        Text(
                            text = "Không có danh mục trong Another Saved List.",
                            color = Color(0xFF888888),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        CollectionsList(data = anotherSavedListData, isGridView = isGridView)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search your bookmark", color = Color(0xFF888888)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1E90FF),
            unfocusedBorderColor = Color(0xFF888888),
            cursorColor = Color(0xFF1E90FF)
        ),
        leadingIcon = {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_search),
                contentDescription = "Search Icon",
                tint = Color(0xFF888888)
            )
        },
        readOnly = true
    )
}

@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Expand Icon",
                tint = Color(0xFF1E90FF),
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }
        if (expanded) {
            content()
        }
    }
}

data class CollectionItemData(val name: String, val count: Int, val emoji: String)

@Composable
fun CollectionsList(data: List<CollectionItemData>, isGridView: Boolean) {
    if (isGridView) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            data.forEach { item ->
                CollectionItemGrid(item)
            }
        }
    } else {
        LazyColumn {
            items(data) { item ->
                CollectionItemList(item)
            }
        }
    }
}

@Composable
fun CollectionItemGrid(item: CollectionItemData) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFFFFF1DC))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Unspecified
        )
        Text(
            text = item.emoji,
            fontSize = 20.sp,
            modifier = Modifier.offset(x = 20.dp, y = (-20).dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Text(text = "${item.count} item${if (item.count != 1) "s" else ""}", fontSize = 12.sp, color = Color(0xFF888888))
    }
}

@Composable
fun CollectionItemList(item: CollectionItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(text = "${item.count} item${if (item.count != 1) "s" else ""}", fontSize = 12.sp, color = Color(0xFF888888))
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "More",
            tint = Color(0xFF888888),
            modifier = Modifier.clickable { /* Handle ellipsis */ }
        )
    }
}