package com.example.locket.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.locket.ui.theme.LocketYellow
import com.example.locket.ui.theme.ButtonDarkGray

// Data class giả lập cho Friend
data class FriendUI(val id: String, val name: String, val avatar: String = "")

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateToFriend: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uploadState by viewModel.uploadState.collectAsState()
    val scope = rememberCoroutineScope()

    // --- PERMISSION & CAMERA STATE ---
    var hasPermission by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // --- FOCUS EFFECT STATE ---
    var focusRingPosition by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    // --- REVIEW MODE STATE ---
    var messageText by remember { mutableStateOf("") }
    var showTimeOverlay by remember { mutableStateOf(false) }
    var showLocationOverlay by remember { mutableStateOf(false) }

    val mockFriends = remember {
        listOf(
            FriendUI("all", "All"),
            FriendUI("1", "Alice"),
            FriendUI("2", "Bob"),
            FriendUI("3", "Charlie"),
            FriendUI("4", "David"),
            FriendUI("5", "Eve")
        )
    }
    var selectedFriendId by remember { mutableStateOf("all") }

    val currentTimeStr = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentLocation = "Hanoi, VN"

    // --- IMAGE CAPTURE SETUP ---
    val imageCapture = remember {
        ImageCapture.Builder()
            .setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()
    }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isFlashOn) {
        imageCapture.flashMode = if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            Toast.makeText(context, "Đã gửi ảnh thành công!", Toast.LENGTH_SHORT).show()
            capturedImageUri = null
            capturedFile = null
            messageText = ""
            showTimeOverlay = false
            showLocationOverlay = false
            selectedFriendId = "all"
            viewModel.resetState()
        } else if (uploadState is UploadState.Error) {
            Toast.makeText(context, (uploadState as UploadState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
    }

    if (!hasPermission) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Cần quyền Camera để tiếp tục", color = Color.White)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 40.dp, bottom = 20.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // SỬA: Kéo từ dưới lên (dragAmount < -20) để mở History
                    if (dragAmount < -20) {
                        onNavigateToHistory()
                    }
                }
            }
    ) {
        if (capturedImageUri == null) {
            // ==========================================
            // 1. CAMERA PREVIEW MODE
            // ==========================================

            // --- TOP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconCircle(Icons.Rounded.Person) { onNavigateToProfile() }
                Button(
                    onClick = { onNavigateToFriend() },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonDarkGray),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add a Friend", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                SmallIconCircle(Icons.Rounded.ChatBubble) { /* TODO Chat */ }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- CAMERA VIEW ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            camera?.let { cam ->
                                val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                                val maxZoom = cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                                val minZoom = cam.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                                val newZoom = (currentZoom * zoom).coerceIn(minZoom, maxZoom)
                                cam.cameraControl.setZoomRatio(newZoom)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                previewView?.let { view ->
                                    val meteringPointFactory = view.meteringPointFactory
                                    val point = meteringPointFactory.createPoint(offset.x, offset.y)
                                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                        .build()
                                    camera?.cameraControl?.startFocusAndMetering(action)
                                    focusRingPosition = offset
                                    showFocusRing = true
                                    scope.launch {
                                        delay(1000)
                                        showFocusRing = false
                                    }
                                }
                            }
                        )
                    }
            ) {
                AndroidView(
                    factory = { ctx ->
                        val view = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        }
                        previewView = view
                        val executor = ContextCompat.getMainExecutor(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                            try {
                                provider.unbindAll()
                                camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, executor)
                        view
                    },
                    modifier = Modifier.fillMaxSize()
                )

                val currentZoomState = camera?.cameraInfo?.zoomState?.value
                val zoomLevel = currentZoomState?.zoomRatio ?: 1f
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).size(36.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%.1fx", zoomLevel),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showFocusRing && focusRingPosition != null) {
                    val focusColor = LocketYellow
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = focusColor, radius = 40f, center = focusRingPosition!!, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                    }
                }
            }
            DisposableEffect(Unit) { onDispose { cameraProvider?.unbindAll() } }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTTOM CONTROLS ---
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isFlashOn = !isFlashOn }) {
                        Icon(if(isFlashOn) Icons.Rounded.FlashOn else Icons.Default.FlashOff, "Flash", tint = if(isFlashOn) LocketYellow else Color.White, modifier = Modifier.size(28.dp))
                    }
                    Box(
                        modifier = Modifier.size(80.dp).border(4.dp, LocketYellow, CircleShape).padding(6.dp).clip(CircleShape).background(Color.White)
                            .clickable { takePhoto(context, imageCapture) { file, uri -> capturedFile = file; capturedImageUri = uri } }
                    )
                    IconButton(onClick = { toggleCamera() }) {
                        Icon(Icons.Rounded.FlipCameraIos, "Switch", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToHistory() }) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = Color.White)
                    Text("History", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

        } else {
            // ==========================================
            // 2. REVIEW MODE (SEND IMAGE UI)
            // ==========================================

            // --- TOP BAR REVIEW ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Nút đóng (bên trái)
                IconButton(
                    onClick = {
                        capturedImageUri = null
                        capturedFile = null
                        messageText = ""
                    },
                    modifier = Modifier.background(ButtonDarkGray, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }

                // Nút tải xuống (bên phải)
                IconButton(
                    onClick = { /* TODO: Save to Gallery */ },
                    modifier = Modifier.background(ButtonDarkGray, CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.Download, "Save", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- MAIN IMAGE & OVERLAYS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(1f) // Hình vuông
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    rememberAsyncImagePainter(capturedImageUri),
                    "Captured",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Time Overlay (Góc trên phải)
                if (showTimeOverlay) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = currentTimeStr, color = LocketYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Location Overlay (Góc dưới trái)
                if (showLocationOverlay) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = LocketYellow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = currentLocation, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- TOOLS ROW (Chọn Icon, Time, Location) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Toggle
                IconButton(onClick = { showTimeOverlay = !showTimeOverlay }) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = if (showTimeOverlay) LocketYellow else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Location Toggle
                IconButton(onClick = { showLocationOverlay = !showLocationOverlay }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (showLocationOverlay) LocketYellow else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // --- MESSAGE INPUT (Dưới ảnh) ---
            TextField(
                value = messageText,
                onValueChange = { if (it.length <= 100) messageText = it },
                placeholder = {
                    Text("Add a message...", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = LocketYellow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- SEND BUTTON (Giữa) ---
            // SỬA: Thêm fillMaxWidth để nút nằm giữa theo chiều ngang
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        capturedFile?.let { file ->
                            viewModel.uploadImage(
                                file = file,
                                message = if (messageText.isNotBlank()) messageText else null,
                                time = if (showTimeOverlay) currentTimeStr else null,
                                location = if (showLocationOverlay) currentLocation else null
                            )
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(LocketYellow, CircleShape)
                        .border(4.dp, Color.Black.copy(alpha = 0.2f), CircleShape), // Viền nhẹ
                    enabled = uploadState !is UploadState.Loading
                ) {
                    if (uploadState is UploadState.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(30.dp))
                    } else {
                        Icon(Icons.Rounded.ArrowUpward, "Send", tint = Color.Black, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- FRIEND LIST (Trượt ngang dưới cùng) ---
            Text("Send to...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockFriends) { friend ->
                    val isSelected = selectedFriendId == friend.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedFriendId = friend.id }
                            .padding(4.dp)
                    ) {
                        // Avatar Friend
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isSelected) LocketYellow else ButtonDarkGray, CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            if (friend.id == "all") {
                                Icon(Icons.Default.People, null, tint = if (isSelected) Color.Black else Color.White)
                            } else {
                                Text(
                                    text = friend.name.take(1),
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = friend.name,
                            color = if (isSelected) LocketYellow else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ================== CÁC HÀM PHỤ TRỢ (Thêm vào cuối file) ==================

@Composable
fun SmallIconCircle(icon: ImageVector, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).background(ButtonDarkGray, CircleShape).clickable { onClick() }) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCaptured: (File, Uri) -> Unit) {
    val photoFile = File(context.cacheDir, "Locket_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile, Uri.fromFile(photoFile))
            }
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Lỗi: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}