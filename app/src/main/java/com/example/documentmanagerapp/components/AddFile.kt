package com.example.documentmanagerapp.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.result.PickVisualMediaRequest

@Composable
fun AddFileScreen(onBackClick: () -> Unit = {}) {
    var screenMode by remember { mutableStateOf("initial") } // initial, file, media, link
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Chọn thư mục") }
    var password by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Mock categories (replace with API call)
    val categories = listOf("Work", "Personal", "Study")

    // File/Media picker launchers
    val context = LocalContext.current
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.path?.substringAfterLast("/") ?: "Unnamed document"
            title = selectedFileName
        }
    }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.path?.substringAfterLast("/") ?: "Unnamed media"
            title = selectedFileName
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && selectedFileUri != null) {
            selectedFileName = "photo_${System.currentTimeMillis()}.jpg"
            title = selectedFileName
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // Proceed with camera or media picker
        } else {
            // TODO: Show permission denied message (e.g., Snackbar)
        }
    }

    // Reset form function
    fun resetForm() {
        title = ""
        url = ""
        selectedFileUri = null
        selectedFileName = ""
        category = "Chọn thư mục"
        password = ""
        isLoading = false
    }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF5F7FA), Color(0xFFE4E8F0))
                )
            )
    ) {
        when (screenMode) {
            "initial" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1E3A8A)
                            )
                        }
                        Text(
                            text = "Add New File",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Choose Upload Method
                    Text(
                        text = "Choose Upload Method",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Select how you want to add your file",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Upload Methods
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        UploadMethodItem(
                            icon = Icons.Default.Attachment,
                            title = "Upload File",
                            subtitle = "Upload documents from your device",
                            backgroundColor = Color(0xFF3B82F6),
                            onClick = { screenMode = "file" }
                        )
                        UploadMethodItem(
                            icon = Icons.Default.Image,
                            title = "Upload Media",
                            subtitle = "Upload photos or videos from your gallery",
                            backgroundColor = Color(0xFFEF4444),
                            onClick = { screenMode = "media" }
                        )
                        UploadMethodItem(
                            icon = Icons.Default.Link,
                            title = "Add Link",
                            subtitle = "Save a web link or URL to your storage",
                            backgroundColor = Color(0xFF10B981),
                            onClick = { screenMode = "link" }
                        )
                    }
                }
            }
            else -> {
                // Form Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { screenMode = "initial"; resetForm() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1E3A8A)
                            )
                        }
                        Text(
                            text = when (screenMode) {
                                "file" -> "Upload File"
                                "media" -> "Upload Media"
                                else -> "Add Link"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Form
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(15.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Title
                            Text(
                                text = "Title",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7F8C8D)
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF7F8C8D)
                                    )
                                },
                                enabled = !isLoading
                            )

                            // URL (for link mode)
                            if (screenMode == "link") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "URL/Link",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7F8C8D)
                                )
                                OutlinedTextField(
                                    value = url,
                                    onValueChange = {
                                        url = it
                                        selectedFileUri = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = Color(0xFF7F8C8D)
                                        )
                                    },
                                    enabled = !isLoading
                                )
                            }

                            // File/Media Upload
                            if (screenMode == "file" || screenMode == "media") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (screenMode == "file") "Upload File" else "Upload Media",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7F8C8D)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (screenMode == "media") {
                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                                // TODO: Set proper URI for camera (use FileProvider)
                                                selectedFileUri = Uri.parse("file://temp_photo.jpg") // Placeholder
                                                cameraLauncher.launch(selectedFileUri!!)
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                                        ) {
                                            Icon(Icons.Default.Camera, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Capture")
                                        }
                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                                mediaPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B59B6))
                                        )
                                        {
                                            Icon(Icons.Default.Image, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Gallery")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                documentPicker.launch("*/*")
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22))
                                        ) {
                                            Icon(Icons.Default.Folder, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Choose File")
                                        }
                                    }
                                }
                                if (selectedFileUri != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .background(Color(0xFFF8F9FA))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when {
                                            selectedFileName.endsWith(".jpg", ignoreCase = true) ||
                                                    selectedFileName.endsWith(".jpeg", ignoreCase = true) ||
                                                    selectedFileName.endsWith(".png", ignoreCase = true) -> Icons.Default.Image
                                            selectedFileName.endsWith(".mp4", ignoreCase = true) -> Icons.Default.Videocam
                                            else -> Icons.Default.Description
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF3498DB)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = selectedFileName,
                                            fontSize = 14.sp,
                                            color = Color(0xFF2C3E50),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Category
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Category",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7F8C8D)
                            )
                            OutlinedButton(
                                onClick = { showCategoryDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                enabled = !isLoading,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF7F8C8D))
                                Spacer(Modifier.width(8.dp))
                                Text(category, color = Color(0xFF2C3E50))
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF7F8C8D))
                            }

                            // Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { screenMode = "initial"; resetForm() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFE74C3C)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE74C3C))
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        if (title.isEmpty()) {
                                            // TODO: Show error (e.g., Snackbar)
                                        } else {
                                            showPasswordDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }

                // Category Dialog
                if (showCategoryDialog) {
                    Dialog(onDismissRequest = { showCategoryDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Select Category",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C3E50)
                                    )
                                    IconButton(onClick = { showCategoryDialog = false }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color(0xFFE74C3C)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                categories.forEach { cat ->
                                    Text(
                                        text = cat,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                category = cat
                                                showCategoryDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        fontSize = 16.sp,
                                        color = Color(0xFF2C3E50)
                                    )
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // Password Dialog
                if (showPasswordDialog) {
                    Dialog(onDismissRequest = { if (!isLoading) showPasswordDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Set Password",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C3E50)
                                    )
                                    IconButton(
                                        onClick = { if (!isLoading) showPasswordDialog = false },
                                        enabled = !isLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color(0xFFE74C3C)
                                        )
                                    }
                                }
                                Text(
                                    text = "Protect your file with a password",
                                    fontSize = 14.sp,
                                    color = Color(0xFF7F8C8D),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Enter password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF7F8C8D)
                                        )
                                    },
                                    enabled = !isLoading,
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                if (isLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }  else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = { showPasswordDialog = false },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = Color(0xFFE74C3C)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE74C3C))
                                        ) {
                                            Text("Cancel")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (password.isEmpty()) {
                                                    // TODO: Show error (e.g., Snackbar)
                                                } else {
                                                    isLoading = true
                                                    // TODO: Implement upload logic
                                                    isLoading = false
                                                    showPasswordDialog = false
                                                    resetForm()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                                        ) {
                                            Text("Confirm")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UploadMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
        }
    }
}