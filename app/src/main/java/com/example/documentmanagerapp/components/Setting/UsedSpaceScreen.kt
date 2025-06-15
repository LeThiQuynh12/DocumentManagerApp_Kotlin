package com.example.documentmanagerapp.components.Setting
import androidx.lifecycle.ViewModelProvider
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.documentmanagerapp.components.context.AuthViewModelFactory
import com.example.documentmanagerapp.context.AuthViewModel
import com.example.documentmanagerapp.utils.User
import com.example.documentmanagerapp.utils.repository.FileRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// Data class for folder data
data class FolderData(
    val label: String,
    val size: Long,
    val color: String,
    val percent: Float
)

// Data class for formatted size
data class FormattedSize(val value: String, val unit: String)

// Utility function to format size
fun formatSize(sizeInBytes: Long): FormattedSize {
    if (sizeInBytes <= 0) return FormattedSize("0", "B")
    val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
    return when {
        sizeInMB < 1 -> FormattedSize(String.format("%.2f", sizeInMB * 1024), "KB")
        sizeInMB < 1024 -> FormattedSize(String.format("%.2f", sizeInMB), "MB")
        else -> FormattedSize(String.format("%.2f", sizeInMB / 1024), "GB")
    }
}

// Utility function to generate random color
fun generateRandomColor(): String {
    val letters = "0123456789ABCDEF"
    return buildString {
        append("#")
        repeat(6) {
            append(letters[Random.nextInt(16)])
        }
    }
}

// ViewModel for UsedSpace
class UsedSpaceViewModel(
    private val context: Context,
    private val authViewModel: AuthViewModel,
    private val fileRepository: FileRepository
) : ViewModel() {
    private val _folderData = mutableStateOf<List<FolderData>?>(null)
    val folderData: List<FolderData>? get() = _folderData.value
    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value
    private val _error = mutableStateOf<String?>(null)
    val error: String? get() = _error.value

    init {
        fetchFolderSizes()
    }

    fun fetchFolderSizes() {
        viewModelScope.launch {
            _loading.value = true
            val user = authViewModel.user.value
            if (user == null || user.id == 0L) {
                _loading.value = false
                _error.value = "Vui lòng đăng nhập để xem dung lượng."
                return@launch
            }
            try {
                val data = fileRepository.getFolderSizes("${user.id}/")
                val isUserRole = user.role == "USER"
                val maxSize = if (isUserRole) 100 * 1024 * 1024L else null // 100 MB
                val totalSize = data.values.sum()
                val folders = data.entries
                    .map { (key, size) ->
                        FolderData(
                            label = key.replace(Regex("^${user.id}/"), ""),
                            size = size,
                            color = generateRandomColor(),
                            percent = if (isUserRole && maxSize != null) size.toFloat() / maxSize else size.toFloat() / totalSize
                        )
                    }
                    .sortedByDescending { it.size }
                    .toMutableList()
                if (isUserRole && maxSize != null && totalSize < maxSize) {
                    folders.add(
                        FolderData(
                            label = "Remaining Space",
                            size = maxSize - totalSize,
                            color = "#D1D5DB",
                            percent = (maxSize - totalSize).toFloat() / maxSize
                        )
                    )
                }
                _folderData.value = folders
                _error.value = null
            } catch (e: HttpException) {
                _folderData.value = emptyList()
                _error.value = if (e.code() == 401) {
                    "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
                } else {
                    "Lỗi tải dữ liệu dung lượng: HTTP ${e.code()}"
                }
            } catch (e: Exception) {
                _folderData.value = emptyList()
                _error.value = "Lỗi tải dữ liệu dung lượng: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<FolderData>,
    size: Float = 350f,
    strokeWidth: Float = 28f,
    gap: Float = 0.01f,
    modifier: Modifier = Modifier
) {


    Canvas(modifier = modifier.size(size.dp)) {
        val total = data.sumOf { it.size.toDouble() }.toFloat()
        var startAngle = -90f
        val diameter = size - strokeWidth
        val topLeft = Offset(
            (this.size.width - diameter) / 2f,
            (this.size.height - diameter) / 2f
        )

        data.forEach { item ->
            val sweepAngle = (item.size / total) * (360f * (1f - gap))
            drawArc(
                color = Color(android.graphics.Color.parseColor(item.color)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle + (360f * gap / data.size)
        }
    }

}

@Composable
fun StorageCard(
    label: String,
    sizeInBytes: Long,
    progress: Float,
    color: String,
    modifier: Modifier = Modifier
) {
    val formattedSize = formatSize(sizeInBytes)
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(android.graphics.Color.parseColor(color)))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${formattedSize.value} ${formattedSize.unit}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                LinearProgressIndicator(
                    progress = { min(max(progress, 0f), 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 5.dp),
                    color = Color(android.graphics.Color.parseColor(color)),
                    trackColor = Color(0xFFE5E7EB)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.2.dp)
                .background(Color(0xFFE9E9E9))
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun UsedSpaceScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val user by authViewModel.user.observeAsState(initial = null)

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val fileRepository = FileRepository(context)
    val viewModel: UsedSpaceViewModel = viewModel(
        factory = UsedSpaceViewModelFactory(context, authViewModel, fileRepository)
    )

    val isUserRole = user!!.role == "USER"
    val maxSize = if (isUserRole) 100 * 1024 * 1024L else null // 100 MB
    val maxSizeInMB = maxSize?.div(1024.0 * 1024.0)

    if (viewModel.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(viewModel.error ?: "Đã xảy ra lỗi.")
                if (viewModel.error?.contains("Phiên đăng nhập hết hạn") == true) {
                    Button(onClick = { navController.navigate("login") }) {
                        Text("Đăng nhập lại")
                    }
                }
            }
        }
        return
    }

    if (viewModel.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (viewModel.folderData.isNullOrEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("No folders found.")
        }
        return
    }

    val totalSizeInBytes = viewModel.folderData?.sumOf { it.size } ?: 0L
    val formattedTotalSize = formatSize(totalSizeInBytes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                text = "Used Space",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.width(24.dp))
        }




        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // DonutChart được căn giữa
            DonutChart(
                data = viewModel.folderData!!.map { item ->
                    FolderData(
                        label = item.label,
                        size = item.size,
                        color = item.color,
                        percent = if (isUserRole && maxSize != null)
                            item.size.toFloat() / maxSize
                        else
                            item.size.toFloat() / totalSizeInBytes
                    )
                },
                size = 380f,
                strokeWidth = 28f,
                gap = 0.01f,
                modifier = Modifier.align(Alignment.Center) // quan trọng để nó không lệch
            )

            // Text ở giữa vòng tròn
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "${formattedTotalSize.value} ${formattedTotalSize.unit}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
                Text(
                    text = "of ${if (isUserRole) "$maxSizeInMB MB" else "Unlimited"}",
                    fontSize = 12.sp,
                    color = Color(0xFF1F2937)
                )
            }
        }






        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            viewModel.folderData?.forEach { item ->
                StorageCard(
                    label = item.label,
                    sizeInBytes = item.size,
                    progress = if (isUserRole && maxSize != null) item.size.toFloat() / maxSize else item.size.toFloat() / totalSizeInBytes,
                    color = item.color
                )
            }
        }
    }
}

class UsedSpaceViewModelFactory(
    private val context: Context,
    private val authViewModel: AuthViewModel,
    private val fileRepository: FileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsedSpaceViewModel::class.java)) {
            return UsedSpaceViewModel(context, authViewModel, fileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}