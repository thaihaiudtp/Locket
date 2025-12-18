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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import com.example.locket.ui.theme.LocketYellow
import com.example.locket.ui.theme.ButtonDarkGray

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit,
    onNavigateToFriend: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uploadState by viewModel.uploadState.collectAsState()
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // State cho hiệu ứng Focus
    var focusRingPosition by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

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
                    if (dragAmount < -20) {
                        onNavigateToHistory()
                    }
                }
            }
    ) {
        if (capturedImageUri == null) {
            // === 1. TOP BAR ===
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconCircle(Icons.Rounded.Person) { /* TODO */ }
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
                SmallIconCircle(Icons.Rounded.ChatBubble) { /* TODO */ }
            }

            Spacer(modifier = Modifier.weight(1f))

            // === 2. CAMERA PREVIEW ===
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
                            // Đã XÓA phần onDoubleTap
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

                // Hiển thị mức Zoom
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

                // Hiển thị vòng tròn Focus bằng Canvas để chính xác vị trí chạm
                if (showFocusRing && focusRingPosition != null) {
                    val focusColor = LocketYellow
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = focusColor,
                            radius = 40f,
                            center = focusRingPosition!!,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                }
            }
            DisposableEffect(Unit) { onDispose { cameraProvider?.unbindAll() } }

            Spacer(modifier = Modifier.weight(1f))

            // === 3. BOTTOM CONTROLS ===
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigateToHistory() }
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = Color.White)
                    Text("History", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

        } else {
            // === 4. REVIEW MODE ===
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).aspectRatio(1f).clip(RoundedCornerShape(32.dp))
            ) {
                Image(rememberAsyncImagePainter(capturedImageUri), "Captured", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { capturedImageUri = null; capturedFile = null }, modifier = Modifier.size(50.dp).background(ButtonDarkGray, CircleShape)) {
                    Icon(Icons.Default.Close, "Retake", tint = Color.White)
                }
                IconButton(onClick = { capturedFile?.let { viewModel.uploadImage(it) } }, modifier = Modifier.size(80.dp).background(LocketYellow, CircleShape), enabled = uploadState !is UploadState.Loading) {
                    if (uploadState is UploadState.Loading) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(30.dp))
                    else Icon(Icons.Rounded.Send, "Send", tint = Color.Black, modifier = Modifier.size(40.dp).padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
fun SmallIconCircle(icon: ImageVector, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).background(ButtonDarkGray, CircleShape).clickable { onClick() }) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
private fun takePhoto(context: Context, imageCapture: ImageCapture, onImageCaptured: (File, Uri) -> Unit) {
    val photoFile = File(context.cacheDir, "Locket_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) { onImageCaptured(photoFile, Uri.fromFile(photoFile)) }
        override fun onError(exc: ImageCaptureException) { Toast.makeText(context, "Lỗi: ${exc.message}", Toast.LENGTH_SHORT).show() }
    })
}