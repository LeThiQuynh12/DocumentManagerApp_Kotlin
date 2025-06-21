package com.example.documentmanagerapp.components.Collections

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.utils.data.*
import com.example.documentmanagerapp.utils.repository.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.DisposableEffect

class FileDetailsViewModel(
    private val context: Context,
    private val documentRepository: DocumentRepository,
    private val versionRepository: DocumentVersionRepository,
    private val permissionRepository: PermissionRepository,
    private val fileRepository: FileRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    var document by mutableStateOf<DocumentData?>(null)
        private set
    var versions by mutableStateOf<List<DocumentVersionData>>(emptyList())
        private set
    var permissions by mutableStateOf<List<PermissionData>>(emptyList())
        private set
    var selectedVersion by mutableStateOf<DocumentVersionData?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var presignedUrl by mutableStateOf<String?>(null)
        private set

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)

    fun updateLoading(isLoading: Boolean) {
        loading = isLoading
    }

    fun updateError(message: String?) {
        error = message
    }

    fun fetchDocumentDetails(documentId: Long, userId: Long) {
        viewModelScope.launch {
            updateLoading(true)
            updateError(null)
            try {
                val documentDeferred = async { documentRepository.getDocumentById(documentId) }
                val versionsDeferred = async { versionRepository.getVersionsByDocumentId(documentId) }
                val permissionsDeferred = async { permissionRepository.getPermissionsByDocumentId(documentId) }

                document = documentDeferred.await()
                versions = versionsDeferred.await().sortedByDescending {
                    it.createdAt?.time ?: 0L
                }
                permissions = permissionsDeferred.await().also {
                    Log.d("FileDetailsViewModel", "Permissions fetched: ${it.size} items")
                }
                selectedVersion = versions.firstOrNull()
                Log.d("FileDetailsViewModel", "Fetched document: $document, versions: ${versions.size}, permissions: ${permissions.size}")
            } catch (e: Exception) {
                updateError(when {
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi tải chi tiết tài liệu: ${e.message}"
                })
                Log.e("FileDetailsViewModel", "Error fetching details: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun deleteDocument(
        documentId: Long,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (document?.fileType == "Link") {
                    fileRepository.deleteLinkFile(documentId, password)
                } else {
                    fileRepository.deleteMediaFile(documentId, password)
                }
                Log.d("FileDetailsViewModel", "Deleted document: $documentId")
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Mật khẩu không hợp lệ"
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi xóa tài liệu: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error deleting document: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun downloadFile(
        documentId: Long,
        versionNumber: Int?,
        fileName: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val fileBytes = fileRepository.downloadFile(documentId, password, versionNumber)
                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, document?.fileType?.toMimeType() ?: "application/octet-stream")
                }

                val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    contentValues.put(MediaStore.MediaColumns.DATA, file.absolutePath)
                    contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                }

                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(fileBytes)
                    }
                    Log.d("FileDetailsViewModel", "Downloaded and saved file: $fileName")
                    onSuccess()
                } ?: run {
                    throw Exception("Không thể tạo URI để lưu file")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Mật khẩu không hợp lệ"
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi tải file: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error downloading file: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun viewDocument(
        documentId: Long,
        versionNumber: Int?,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                presignedUrl = fileRepository.getPresignedUrl(documentId, password, versionNumber)
                Log.d("FileDetailsViewModel", "Fetched presigned URL: $presignedUrl")
                onSuccess(presignedUrl!!)
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Mật khẩu không hợp lệ"
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi xem tài liệu: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error viewing document: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun shareDocument(
        documentId: Long,
        email: String,
        permissionType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val permissionTypeEnum = when (permissionType) {
                    "READ" -> PermissionType.READ
                    "EDIT" -> PermissionType.EDIT
                    else -> throw IllegalArgumentException("Loại quyền không hợp lệ: $permissionType")
                }
                permissionRepository.shareDocumentByEmail(ShareRequestData(email, documentId.toString(), permissionTypeEnum))
                Log.d("FileDetailsViewModel", "Chia sẻ tài liệu với $email")
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi chia sẻ tài liệu: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Lỗi khi chia sẻ tài liệu: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun updatePermission(
        documentId: Long,
        email: String,
        permissionType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val permissionTypeEnum = when (permissionType) {
                    "READ" -> PermissionType.READ
                    "EDIT" -> PermissionType.EDIT
                    else -> throw IllegalArgumentException("Loại quyền không hợp lệ: $permissionType")
                }
                permissionRepository.updatePermission(
                    UpdatePermissionRequestData(email, documentId, permissionTypeEnum)
                )
                Log.d("FileDetailsViewModel", "Updated permission for $email")
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi cập nhật quyền: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error updating permission: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun deletePermission(
        permissionId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                permissionRepository.deletePermission(permissionId)
                Log.d("FileDetailsViewModel", "Deleted permission: $permissionId")
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi xóa quyền: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error deleting permission: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun uploadNewVersion(
        documentId: Long,
        fileUri: Uri,
        title: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)
                val fileName = contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } ?: "file"
                val requestBody = inputStream?.readBytes()?.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody!!)

                val uploadResponse = fileRepository.uploadFile(
                    file = filePart,
                    folder = "documents",
                    password = password,
                    userId = authViewModel.user.value?.id ?: 0L,
                    categoryId = document?.category?.id ?: 0L,
                    documentId = documentId
                )

                val newVersion = DocumentVersionData(
                    document = document!!,
                    versionNumber = (versions.maxOfOrNull { it.versionNumber } ?: 0) + 1,
                    s3Url = uploadResponse.s3Url,
                    fileSize = uploadResponse.fileSize,
                    title = title
                )
                versionRepository.createDocumentVersion(newVersion)

                Log.d("FileDetailsViewModel", "Uploaded new version: ${newVersion.versionNumber}")
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("403") == true -> "Mật khẩu không hợp lệ"
                    e.message?.contains("401") == true -> "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                    else -> "Lỗi thêm phiên bản: ${e.message}"
                }
                Log.e("FileDetailsViewModel", "Error uploading new version: ${e.message}")
                onError(errorMessage)
            }
        }
    }

    fun cleanupTempFiles() {
        viewModelScope.launch {
            try {
                fileRepository.deleteTempFile("temp_key") // Thay bằng key thực tế
                Log.d("FileDetailsViewModel", "Cleaned up temporary files")
            } catch (e: Exception) {
                Log.e("FileDetailsViewModel", "Error cleaning up temp files: ${e.message}")
            }
        }
    }

    fun selectVersion(version: DocumentVersionData) {
        selectedVersion = version
        Log.d("FileDetailsViewModel", "Selected version: ${version.versionNumber}")
    }

    private fun String.toMimeType(): String {
        return when (this.lowercase()) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "mp4" -> "video/mp4"
            else -> "application/octet-stream"
        }
    }
}

class FileDetailsViewModelFactory(
    private val context: Context,
    private val documentRepository: DocumentRepository,
    private val versionRepository: DocumentVersionRepository,
    private val permissionRepository: PermissionRepository,
    private val fileRepository: FileRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileDetailsViewModel::class.java)) {
            return FileDetailsViewModel(
                context,
                documentRepository,
                versionRepository,
                permissionRepository,
                fileRepository,
                authViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun FileDetailsScreen(navController: NavController, documentId: Long) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val documentRepository = DocumentRepository(context)
    val versionRepository = DocumentVersionRepository(context)
    val permissionRepository = PermissionRepository(context)
    val fileRepository = FileRepository(context)
    val viewModel: FileDetailsViewModel = viewModel(
        factory = FileDetailsViewModelFactory(
            context,
            documentRepository,
            versionRepository,
            permissionRepository,
            fileRepository,
            authViewModel
        )
    )
    val user by authViewModel.user.observeAsState()
    val document by remember { derivedStateOf { viewModel.document } }
    val versions by remember { derivedStateOf { viewModel.versions } }
    val permissions by remember { derivedStateOf { viewModel.permissions } }
    val selectedVersion by remember { derivedStateOf { viewModel.selectedVersion } }
    val loading by remember { derivedStateOf { viewModel.loading } }
    val error by remember { derivedStateOf { viewModel.error } }
    var showVersionModal by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAddVersionDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var shareEmail by remember { mutableStateOf("") }
    var sharePermissionType by remember { mutableStateOf("READ") }
    var password by remember { mutableStateOf("") }
    var passwordAction by remember { mutableStateOf<String?>(null) }
    var newVersionTitle by remember { mutableStateOf("") }
    var selectedPermission by remember { mutableStateOf<PermissionData?>(null) }
    var newPermissionType by remember { mutableStateOf("READ") }
    var showSharedUsersDialog by remember { mutableStateOf(false) }

    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
    val displayFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale("vi", "VN"))

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.uploadNewVersion(
                documentId = documentId,
                fileUri = it,
                title = newVersionTitle,
                password = document?.password ?: "",
                onSuccess = {
                    Toast.makeText(context, "Thêm phiên bản thành công", Toast.LENGTH_SHORT).show()
                    showAddVersionDialog = false
                    user?.id?.let { userId -> viewModel.fetchDocumentDetails(documentId, userId) }
                },
                onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
            )
        }
    }

    val requestStoragePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            selectedVersion?.let { version ->
                viewModel.downloadFile(
                    documentId = documentId,
                    versionNumber = version.versionNumber,
                    fileName = "${document?.documentName ?: "document"}_${version.versionNumber}",
                    password = if (document?.password != null) password else "",
                    onSuccess = { Toast.makeText(context, "Tải file thành công", Toast.LENGTH_SHORT).show() },
                    onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                )
            }
        } else {
            Toast.makeText(context, "Quyền truy cập bộ nhớ bị từ chối", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(user?.id, documentId) {
        user?.id?.let { userId ->
            viewModel.fetchDocumentDetails(documentId, userId)
        } ?: run {
            viewModel.updateError("Vui lòng đăng nhập để xem chi tiết tài liệu")
            viewModel.updateLoading(false)
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (message.contains("Phiên đăng nhập hết hạn")) {
                try {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi đăng xuất: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupTempFiles()
        }
    }

    AnimatedVisibility(
        visible = showSharedUsersDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showSharedUsersDialog = false },
            title = { Text("Danh sách người được chia sẻ") },
            text = {
                Column {
                    if (permissions.isEmpty()) {
                        Text(
                            text = "Không có người dùng nào được chia sẻ",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        val uniquePermissions = permissions.groupBy { it.user.email }
                            .map { (email, perms) ->
                                perms.maxByOrNull { if (it.permissionType == PermissionType.EDIT) 1 else 0 }!!
                            }
                        Log.d("FileDetailsScreen", "Unique permissions: ${uniquePermissions.size}, details: $uniquePermissions")
                        uniquePermissions.forEach { permission ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedPermission = permission
                                        newPermissionType = permission.permissionType.name
                                        showPermissionDialog = true
                                        showSharedUsersDialog = false
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = permission.user.email,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = permission.permissionType.name,
                                    fontSize = 14.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSharedUsersDialog = false }) {
                    Text("Đóng", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showVersionModal,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showVersionModal = false },
            title = { Text("Chọn phiên bản") },
            text = {
                Column {
                    versions.forEach { version ->
                        Text(
                            text = "Phiên bản ${version.versionNumber} (${version.createdAt?.let { displayFormat.format(it) } ?: "N/A"})",
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectVersion(version)
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
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showDeleteDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            title = { Text("Xóa tài liệu") },
            text = { Text("Bạn có chắc chắn muốn xóa tài liệu ${document?.documentName}? Hành động này không thể hoàn tác.") },
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(
                            documentId = documentId,
                            password = if (document?.password != null) password else "",
                            onSuccess = {
                                Toast.makeText(context, "Xóa tài liệu thành công", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = Color(0xFFFF4C4C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showShareDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Chia sẻ tài liệu") },
            text = {
                Column {
                    OutlinedTextField(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                        label = { Text("Email người nhận") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = shareEmail.isNotEmpty() && !isValidEmail(shareEmail),
                        supportingText = {
                            if (shareEmail.isNotEmpty() && !isValidEmail(shareEmail)) {
                                Text("Email không hợp lệ", color = Color.Red)
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = sharePermissionType == "READ",
                                onClick = { sharePermissionType = "READ" }
                            )
                            Text("Chỉ đọc")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = sharePermissionType == "EDIT",
                                onClick = { sharePermissionType = "EDIT" }
                            )
                            Text("Chỉnh sửa")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (shareEmail.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                        } else if (!isValidEmail(shareEmail)) {
                            Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.shareDocument(
                                documentId = documentId,
                                email = shareEmail,
                                permissionType = sharePermissionType,
                                onSuccess = {
                                    Toast.makeText(context, "Chia sẻ thành công", Toast.LENGTH_SHORT).show()
                                    showShareDialog = false
                                    shareEmail = ""
                                    user?.id?.let { userId ->
                                        Log.d("FileDetailsScreen", "Fetching details for userId: $userId")
                                        viewModel.fetchDocumentDetails(documentId, userId)
                                    } ?: run {
                                        Log.e("FileDetailsScreen", "User ID is null, cannot fetch document details")
                                        Toast.makeText(context, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Chia sẻ", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showPasswordDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Nhập mật khẩu") },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        isError = password.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (password.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                        } else {
                            when (passwordAction) {
                                "download" -> {
                                    viewModel.downloadFile(
                                        documentId = documentId,
                                        versionNumber = selectedVersion?.versionNumber,
                                        fileName = "${document?.documentName ?: "document"}_${selectedVersion?.versionNumber}",
                                        password = password,
                                        onSuccess = { Toast.makeText(context, "Tải file thành công", Toast.LENGTH_SHORT).show() },
                                        onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                                    )
                                }
                                "delete" -> {
                                    viewModel.deleteDocument(
                                        documentId = documentId,
                                        password = password,
                                        onSuccess = {
                                            Toast.makeText(context, "Xóa tài liệu thành công", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        },
                                        onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                                    )
                                }
                                "view" -> {
                                    viewModel.viewDocument(
                                        documentId = documentId,
                                        versionNumber = selectedVersion?.versionNumber,
                                        password = password,
                                        onSuccess = { url ->
                                            navController.navigate("fileViewer/$url/${document?.fileType}")
                                        },
                                        onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                                    )
                                }
                            }
                            showPasswordDialog = false
                            password = ""
                        }
                    }
                ) {
                    Text("Xác nhận", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showAddVersionDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showAddVersionDialog = false },
            title = { Text("Thêm phiên bản mới") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newVersionTitle,
                        onValueChange = { newVersionTitle = it },
                        label = { Text("Tiêu đề phiên bản") },
                        isError = newVersionTitle.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newVersionTitle.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show()
                        } else {
                            pickFileLauncher.launch("*/*")
                        }
                    }
                ) {
                    Text("Chọn tệp", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVersionDialog = false }) {
                    Text("Hủy", color = Color(0xFF1E3A8A))
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showPermissionDialog && selectedPermission != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Quản lý quyền: ${selectedPermission?.user?.email}") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newPermissionType == "READ",
                                onClick = { newPermissionType = "READ" }
                            )
                            Text("Chỉ đọc")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newPermissionType == "EDIT",
                                onClick = { newPermissionType = "EDIT" }
                            )
                            Text("Chỉnh sửa")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updatePermission(
                            documentId = documentId,
                            email = selectedPermission?.user?.email ?: "",
                            permissionType = newPermissionType,
                            onSuccess = {
                                Toast.makeText(context, "Cập nhật quyền thành công", Toast.LENGTH_SHORT).show()
                                showPermissionDialog = false
                                user?.id?.let { userId -> viewModel.fetchDocumentDetails(documentId, userId) }
                            },
                            onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                        )
                    }
                ) {
                    Text("Cập nhật", color = Color(0xFF1E90FF))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePermission(
                            permissionId = selectedPermission!!.id!!,
                            onSuccess = {
                                Toast.makeText(context, "Xóa quyền thành công", Toast.LENGTH_SHORT).show()
                                showPermissionDialog = false
                                user?.id?.let { userId -> viewModel.fetchDocumentDetails(documentId, userId) }
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, "Không thể xóa quyền: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("Xóa quyền", color = Color(0xFFFF4C4C))
                }
            }
        )
    }

    if (loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFF1E3A8A))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang tải chi tiết tài liệu...", color = Color(0xFF1E3A8A))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E3A8A))
            }
            Text(
                text = "Chi tiết tài liệu",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            IconButton(onClick = {
                selectedVersion?.let { version ->
                    if (document?.password != null) {
                        passwordAction = "download"
                        showPasswordDialog = true
                    } else {
                        viewModel.downloadFile(
                            documentId = documentId,
                            versionNumber = version.versionNumber,
                            fileName = "${document?.documentName ?: "document"}_${version.versionNumber}",
                            password = "",
                            onSuccess = { Toast.makeText(context, "Tải file thành công", Toast.LENGTH_SHORT).show() },
                            onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                        )
                    }
                } ?: run {
                    Toast.makeText(context, "Vui lòng chọn một phiên bản để tải", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Default.Download, contentDescription = "Tải xuống", tint = Color(0xFF1E3A8A))
            }
        }

        document?.let { doc ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = doc.documentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "${doc.category?.name?.let { "🌈 $it" } ?: "Chưa phân loại"}",
                        fontSize = 14.sp,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoRow(label = "Chủ sở hữu:", value = doc.user?.email ?: "Không xác định")
                    InfoRow(label = "Phương thức mã hóa:", value = doc.encryptionMethod ?: "Không có")
                    InfoRow(label = "Loại file:", value = doc.fileType.uppercase())
                    InfoRow(
                        label = "Ngày tạo:",
                        value = doc.createdAt?.let { displayFormat.format(it) } ?: "N/A"
                    )
                    InfoRow(
                        label = "Kích thước:",
                        value = selectedVersion?.fileSize?.let { formatFileSize(it) } ?: "0 B"
                    )

                    Text(
                        text = "Chọn phiên bản:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = selectedVersion?.let { "Phiên bản ${it.versionNumber} (${it.createdAt?.let { displayFormat.format(it) } ?: "N/A"})" } ?: "Không có phiên bản",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Chọn phiên bản",
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

                    val uniquePermissions = permissions.groupBy { it.user.email }
                        .map { (email, perms) ->
                            perms.maxByOrNull { if (it.permissionType == PermissionType.EDIT) 1 else 0 }!!
                        }
                    Log.d("FileDetailsScreen", "Unique permissions: ${uniquePermissions.size}, details: $uniquePermissions")
                    Text(
                        text = "Chia sẻ với: ${uniquePermissions.size} người dùng",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { showSharedUsersDialog = true }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionButton(
                            icon = Icons.Default.Visibility,
                            label = "Xem tài liệu",
                            color = Color(0xFF3B82F6),
                            onClick = {
                                if (document?.password != null) {
                                    passwordAction = "view"
                                    showPasswordDialog = true
                                } else {
                                    viewModel.viewDocument(
                                        documentId = documentId,
                                        versionNumber = selectedVersion?.versionNumber,
                                        password = "",
                                        onSuccess = { url ->
                                            navController.navigate("fileViewer/$url/${document?.fileType}")
                                        },
                                        onError = { errorMessage -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() }
                                    )
                                }
                            }
                        )
                        ActionButton(
                            icon = Icons.Default.Delete,
                            label = "Xóa",
                            color = Color(0xFFFF4C4C),
                            onClick = {
                                if (document?.password != null) {
                                    passwordAction = "delete"
                                    showPasswordDialog = true
                                } else {
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionButton(
                            icon = Icons.Default.Share,
                            label = "Chia sẻ",
                            color = Color(0xFFFFA500),
                            onClick = { showShareDialog = true }
                        )
                        ActionButton(
                            label = "Thêm phiên bản",
                            color = Color(0xFF10B981),
                            onClick = { showAddVersionDialog = true }
                        )
                    }
                }
            }
        } ?: run {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Không tìm thấy tài liệu",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Button(
                    onClick = { user?.id?.let { viewModel.fetchDocumentDetails(documentId, it) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                ) {
                    Text("Thử lại", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FileViewerScreen(url: String, fileType: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFF1E3A8A))
            }
            Text(
                text = "Xem tài liệu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        when (fileType.lowercase()) {
            "pdf" -> {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            "png", "jpg", "jpeg" -> {
                AsyncImage(
                    model = url,
                    contentDescription = "Hình ảnh tài liệu",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            "mp4" -> {
                Text("Phát video chưa được triển khai", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> {
                Text(
                    "Loại tệp không được hỗ trợ: $fileType",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(45.dp)
            .padding(horizontal = 4.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(label, color = Color.White)
    }
}

fun isValidEmail(email: String): Boolean {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())
}

fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return String.format("%.2f %s", size, units[unitIndex])
}