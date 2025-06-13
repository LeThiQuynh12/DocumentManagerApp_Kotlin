
package com.example.documentmanagerapp.components.Collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailsScreen(navController: NavController, documentId: Long) {
    val dropdownOptions = listOf("Version 6 (6/6/2025)", "Version 5 (6/5/2025)")
    var selectedVersion by remember { mutableStateOf(dropdownOptions[0]) }
    var showVersionModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Document Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            IconButton(onClick = { /* Handle download */ }) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }


        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BaiThuHoach.docx",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Text(
                    text = "🌈 Triển",
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                InfoRow(label = "Owner:", value = "admin@admin.com")
                InfoRow(label = "Encryption method:", value = "AES")
                InfoRow(label = "File type:", value = "docx")
                InfoRow(label = "Created at:", value = "6/5/2025, 10:50:55 PM")
                InfoRow(label = "Size:", value = "1.2 MB")

                Text(
                    text = "Select version:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )

                OutlinedTextField(
                    value = selectedVersion,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Version",
                            modifier = Modifier.clickable { showVersionModal = true }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledPlaceholderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showVersionModal) {
                    AlertDialog(
                        onDismissRequest = { showVersionModal = false },
                        title = { Text("Select Version") },
                        text = {
                            Column {
                                dropdownOptions.forEach { option ->
                                    Text(
                                        text = option,
                                        modifier = Modifier
                                            .clickable {
                                                selectedVersion = option
                                                showVersionModal = false
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showVersionModal = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Text(
                    text = "Shared with: 1 user",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionButton(icon = Icons.Default.Visibility, label = "View Document", color = Color(0xFF3B82F6))
                    ActionButton(icon = Icons.Default.Delete, label = "Delete", color = Color(0xFFFF4C4C))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionButton(icon = Icons.Default.Share, label = "Share", color = Color(0xFFFFA500))
                    ActionButton(label = "Add Version", color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector? = null,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { /* TODO */ },
        colors = ButtonDefaults.outlinedButtonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(45.dp).padding(horizontal = 4.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(label, color = Color.White)
    }
}