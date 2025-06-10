import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.grid.*


class CollectionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollectionsScreen(navController = rememberNavController())
        }
    }
}

@Composable
fun CollectionsScreen(navController: NavController) {
    CollectionsContent(navController)
}

@Composable
fun CollectionsContent(navController: NavController) {
    var isMainBoosterExpanded by remember { mutableStateOf(true) }
    var isAnotherSavedListExpanded by remember { mutableStateOf(false) }

    val collections = listOf(
        CollectionItem("Inspiration", "52 items"),
        CollectionItem("Catboosters", "165 items"),
        CollectionItem("Brain Foods", "26 items"),
        CollectionItem("Conspiration", "30 items")
    )

    val anotherSavedList = listOf(
        CollectionItem("Travel Memories", "20 items"),
        CollectionItem("Work Notes", "45 items"),
        CollectionItem("Personal Goals", "15 items")
    )

    val emojis = listOf("🌈", "😺", "🧠", "🛸")
    val anotherEmojis = listOf("✈️", "💼", "🎯")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp),
        contentPadding = PaddingValues(bottom = 50.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collections",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(vertical = 15.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = { /* Handle add */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                    IconButton(onClick = { /* Handle swap */ }) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Sort",
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.rotate(90f)
                        )
                    }
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color(0xFF1E3A8A)
                        )
                    }
                    IconButton(onClick = { /* Handle grid */ }) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Grid",
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
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    BasicTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        enabled = false,
                        decorationBox = { innerTextField ->
                            Box {
                                Text(
                                    "Search your bookmark",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 16.sp
                                )
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }

        item {
            // Main Booster Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMainBoosterExpanded = !isMainBoosterExpanded }
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Main Booster",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand/Collapse",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isMainBoosterExpanded) 0f else 180f)
                )
            }
        }

        if (isMainBoosterExpanded) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    collections.chunked(2).forEach { pair ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth()
                        ) {
                            pair.forEachIndexed { index, item ->
                                CollectionCard(
                                    item = item,
                                    emoji = emojis[collections.indexOf(item) % emojis.size],
                                    modifier = Modifier
                                        .padding(bottom = 15.dp, end = if (pair.size > 1) 8.dp else 0.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Another Saved List Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAnotherSavedListExpanded = !isAnotherSavedListExpanded }
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Another Saved List",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand/Collapse",
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isAnotherSavedListExpanded) 0f else 180f)
                )
            }
        }

        if (isAnotherSavedListExpanded) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    anotherSavedList.chunked(2).forEach { pair ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth()
                        ) {
                            pair.forEachIndexed { index, item ->
                                CollectionCard(
                                    item = item,
                                    emoji = anotherEmojis[anotherSavedList.indexOf(item) % anotherEmojis.size],
                                    modifier = Modifier
                                        .padding(bottom = 15.dp, end = if (pair.size > 1) 8.dp else 0.dp)
                                )
                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun CollectionCard(item: CollectionItem, emoji: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(170.dp, 160.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Color(0xFFFFF1DC))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📁",
                fontSize = 48.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 45.dp, end = 60.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 15.dp)
                    .weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = item.count,
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            IconButton(
                onClick = { /* Handle ellipsis */ },
                modifier = Modifier.padding(5.dp)
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

data class CollectionItem(val title: String, val count: String)

