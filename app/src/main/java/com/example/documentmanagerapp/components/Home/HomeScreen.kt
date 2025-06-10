//package com.example.documentmanagerapp.components.Home
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import com.example.documentmanagerapp.R
//import com.example.documentmanagerapp.components.api.viewmodel.DocumentViewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
//@Composable
//fun HomeScreen(navController: NavHostController, documentViewModel: DocumentViewModel = viewModel()) {
//    val onSearchClick = {
//        navController.navigate("search")
//    }
//    // Gọi fetch dữ liệu khi HomeScreen được gọi lần đầu
//    LaunchedEffect(Unit) {
//        documentViewModel.fetchAllDocuments()
//    }
//
//    // Lấy danh sách tài liệu từ ViewModel
//    val documents = documentViewModel.documents.value
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F7FA))
//            .padding(20.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        // ... Phần UI cũ của bạn ...
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Hiển thị danh sách documents lấy từ API
//        Text(
//            text = "Documents from API:",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1E3A8A),
//            modifier = Modifier.padding(bottom = 10.dp)
//        )
//
//        // Nếu danh sách rỗng thì hiện "No documents"
//        if (documents.isEmpty()) {
//            Text("No documents found", color = Color.Gray)
//        } else {
//            // Ví dụ hiển thị tên document trong cột
//            Column {
//                documents.forEach { doc ->
//                    Text(text = doc.documentName, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
//                }
//            }
//        }
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F7FA))
//            .padding(20.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        Text(
//            text = "Hello, John Doe!",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1E3A8A),
//            modifier = Modifier.padding(top = 40.dp, bottom = 20.dp)
//        )
//
//        // Search bar
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//                .background(Color.White, shape = MaterialTheme.shapes.medium)
//                .padding(horizontal = 12.dp)
//                .clickable {
//                    navController.navigate("search")
//                },
//            verticalAlignment = Alignment.CenterVertically
//        ){
//            Icon(
//                painter = painterResource(R.drawable.ic_search),
//                contentDescription = null,
//                tint = Color(0xFF888888),
//                modifier = Modifier.size(24.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = "Search your bookmark",
//                color = Color(0xFF888888),
//                fontSize = 16.sp
//            )
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Categories
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            CategoryButton("Links", R.drawable.ic_link, Color(0xFFE6E6FA), Color(0xFF6A5ACD))
//            CategoryButton("Images", R.drawable.ic_image, Color(0xFFE0F7FA), Color(0xFF00CED1))
//            CategoryButton("Documents", R.drawable.ic_document, Color(0xFFFFE4E1), Color(0xFFFF6347))
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // My Collections
//        SectionHeader(title = "My Collections", seeAllText = "See All 〉")
//
//        LazyRow {
//            items(3) { index ->
//                when (index) {
//                    0 -> CollectionItem(
//                        "Inspiration",
//                        52,
//                        R.drawable.ic_folder,
//                        R.drawable.ic_palette,
//                        Color(0xFFFFD700),
//                        Color(0xFFFF69B4)
//                    )
//                    1 -> CollectionItem(
//                        "Catboosters",
//                        147,
//                        R.drawable.ic_folder,
//                        R.drawable.ic_heart,
//                        Color(0xFFFFD700),
//                        Color(0xFFFF4500)
//                    )
//                    2 -> CollectionItem(
//                        "Brain Foods",
//                        26,
//                        R.drawable.ic_folder,
//                        R.drawable.ic_leaf,
//                        Color(0xFFFFD700),
//                        Color(0xFF32CD32)
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Text(
//            text = "Recent Bookmark",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1E3A8A),
//            modifier = Modifier.padding(bottom = 10.dp)
//        )
//
//        // Recent Bookmark
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.White, shape = MaterialTheme.shapes.medium)
//                .padding(10.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.ic_document),
//                contentDescription = null,
//                modifier = Modifier.size(40.dp),
//                tint = Color(0xFF888888)
//            )
//            Spacer(modifier = Modifier.width(10.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Top UI/UX Design Works for Inspiration",
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp
//                )
//                Text(
//                    text = "UI & UX Design Inspiration",
//                    fontSize = 12.sp,
//                    color = Color(0xFF888888)
//                )
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        painter = painterResource(R.drawable.ic_palette),
//                        contentDescription = null,
//                        modifier = Modifier.size(14.dp),
//                        tint = Color(0xFFFF69B4)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Inspiration • 12:21",
//                        fontSize = 12.sp,
//                        color = Color(0xFF888888)
//                    )
//                }
//            }
//            Icon(
//                imageVector = Icons.Default.MoreVert,
//                contentDescription = null,
//                tint = Color(0xFF888888)
//            )
//        }
//    }
//}
//
//@Composable
//fun CategoryButton(label: String, iconId: Int, bgColor: Color, iconColor: Color) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .padding(horizontal = 5.dp)
//            .background(bgColor, shape = MaterialTheme.shapes.medium)
//            .padding(15.dp)
//    ) {
//        Icon(
//            painter = painterResource(id = iconId),
//            contentDescription = null,
//            tint = iconColor,
//            modifier = Modifier.size(30.dp)
//        )
//        Spacer(modifier = Modifier.height(5.dp))
//        Text(
//            text = label,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color(0xFF333333)
//        )
//    }
//}
//
//@Composable
//fun SectionHeader(title: String, seeAllText: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 10.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = title,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF1E3A8A)
//        )
//        Text(
//            text = seeAllText,
//            fontSize = 14.sp,
//            color = Color(0xFF1E90FF)
//        )
//    }
//}
//
//@Composable
//fun CollectionItem(
//    name: String,
//    count: Int,
//    folderIcon: Int,
//    overlayIcon: Int,
//    folderColor: Color,
//    overlayColor: Color
//) {
//    Column(
//        modifier = Modifier
//            .width(110.dp)
//            .padding(horizontal = 5.dp)
//            .background(Color.White, shape = MaterialTheme.shapes.medium)
//            .padding(10.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box {
//            Icon(
//                painter = painterResource(folderIcon),
//                contentDescription = null,
//                tint = folderColor,
//                modifier = Modifier.size(40.dp)
//            )
//            Icon(
//                painter = painterResource(overlayIcon),
//                contentDescription = null,
//                tint = overlayColor,
//                modifier = Modifier
//                    .size(20.dp)
//                    .align(Alignment.BottomEnd)
//            )
//        }
//        Spacer(modifier = Modifier.height(5.dp))
//        Text(
//            text = name,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color(0xFF333333)
//        )
//        Text(
//            text = "$count Items",
//            fontSize = 12.sp,
//            color = Color(0xFF888888)
//        )
//    }
//}

package com.example.documentmanagerapp.components.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.documentmanagerapp.R
import com.example.documentmanagerapp.components.api.viewmodel.DocumentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(navController: NavHostController, documentViewModel: DocumentViewModel = viewModel()) {
    val onSearchClick = {
        navController.navigate("search")
    }
    // Gọi fetch dữ liệu khi HomeScreen được gọi lần đầu
    LaunchedEffect(Unit) {
        documentViewModel.fetchAllDocuments()
    }

    // Lấy danh sách tài liệu từ ViewModel
    val documents = documentViewModel.documents.value

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
        SectionHeader(title = "My Collections", seeAllText = "See All 〉")

        LazyRow {
            items(3) { index ->
                when (index) {
                    0 -> CollectionItem(
                        "Inspiration",
                        52,
                        R.drawable.ic_folder,
                        R.drawable.ic_palette,
                        Color(0xFFFFD700),
                        Color(0xFFFF69B4)
                    )
                    1 -> CollectionItem(
                        "Catboosters",
                        147,
                        R.drawable.ic_folder,
                        R.drawable.ic_heart,
                        Color(0xFFFFD700),
                        Color(0xFFFF4500)
                    )
                    2 -> CollectionItem(
                        "Brain Foods",
                        26,
                        R.drawable.ic_folder,
                        R.drawable.ic_leaf,
                        Color(0xFFFFD700),
                        Color(0xFF32CD32)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Recent Bookmark",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Recent Bookmark
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_document),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF888888)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Top UI/UX Design Works for Inspiration",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "UI & UX Design Inspiration",
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
                        text = "Inspiration • 12:21",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = Color(0xFF888888)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hiển thị danh sách documents lấy từ API
        Text(
            text = "Documents from API:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Nếu danh sách rỗng thì hiện "No documents"
        if (documents.isEmpty()) {
            Text("No documents found", color = Color.Gray)
        } else {
            // Ví dụ hiển thị tên document trong cột
            Column {
                documents.forEach { doc ->
                    Text(text = doc.documentName, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryButton(label: String, iconId: Int, bgColor: Color, iconColor: Color) {
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
fun SectionHeader(title: String, seeAllText: String) {
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
        Text(
            text = seeAllText,
            fontSize = 14.sp,
            color = Color(0xFF1E90FF)
        )
    }
}

@Composable
fun CollectionItem(
    name: String,
    count: Int,
    folderIcon: Int,
    overlayIcon: Int,
    folderColor: Color,
    overlayColor: Color
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .padding(horizontal = 5.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                painter = painterResource(folderIcon),
                contentDescription = null,
                tint = folderColor,
                modifier = Modifier.size(40.dp)
            )
            Icon(
                painter = painterResource(overlayIcon),
                contentDescription = null,
                tint = overlayColor,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
        Text(
            text = "$count Items",
            fontSize = 12.sp,
            color = Color(0xFF888888)
        )
    }
}