package com.example.locket.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.locket.ui.detail.DetailState
import com.example.locket.ui.detail.DetailViewModel
import com.example.locket.ui.theme.LocketYellow
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackToCamera: () -> Unit
) {
    val pictures by viewModel.pictures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Mặc định là Detail Mode (isGridMode = false)
    var isGridMode by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { pictures.size })
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Logic nút Back cứng của điện thoại
    BackHandler {
        if (isGridMode) {
            // Đang ở Grid -> Back về Detail
            isGridMode = false
        } else {
            // Đang ở Detail -> Back về Camera (Đóng History)
            onBackToCamera()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isGridMode) {
            // ================== 1. GRID VIEW ==================
            Column(Modifier.fillMaxSize()) {
                // Header Grid
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All Memories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(
                        onClick = { isGridMode = false }, // Quay lại detail
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }
                }

                // Grid Content
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(pictures) { index, picture ->
                        // Load more logic
                        if (index == pictures.lastIndex && !isLoading) {
                            LaunchedEffect(Unit) { viewModel.loadNextPage() }
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                                .clickable {
                                    // Click ảnh -> Mở Detail đúng vị trí đó
                                    scope.launch {
                                        pagerState.scrollToPage(index)
                                        isGridMode = false
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = picture.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        } else {
            // ================== 2. DETAIL VIEW (DEFAULT) ==================
            // Vertical Pager (Giống TikTok)
            if (pictures.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No photos yet", color = Color.Gray)
                    Button(onClick = onBackToCamera, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Take a photo")
                    }
                }
            } else {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pictureData = pictures.getOrNull(page)
                    if (pictureData != null) {
                        // Key quan trọng để mỗi page có 1 ViewModel riêng biệt
                        key(pictureData.id) {
                            HistoryDetailItem(
                                pictureId = pictureData.id,
                                initialUrl = pictureData.url, // Truyền url để hiện ngay lập tức
                                initialUsername = pictureData.uploader.username,
                                onSwitchToGrid = { isGridMode = true }, // Nút Grid
                                onNavigateToCamera = onBackToCamera // Nút tròn to
                            )
                        }
                    }
                }
            }
        }

        // Global Loading
        if (isLoading && pictures.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocketYellow)
            }
        }
    }
}

// ================== UI DETAIL ITEM (Copy from your Request) ==================
@Composable
fun HistoryDetailItem(
    pictureId: String,
    initialUrl: String,
    initialUsername: String,
    onSwitchToGrid: () -> Unit,
    onNavigateToCamera: () -> Unit,
    // Inject ViewModel cho từng item
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsState()

    // Load detail khi item hiển thị
    LaunchedEffect(pictureId) {
        viewModel.loadPictureDetail(pictureId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. TOP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(Color.DarkGray, CircleShape)) {
                Icon(Icons.Rounded.Person, null, tint = Color.White)
            }
            Box(modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Everyone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(Color.DarkGray, CircleShape)) {
                Icon(Icons.Rounded.ChatBubble, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 2. MAIN CONTENT (HÌNH VUÔNG) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(1f)
        ) {
            // Ảnh nền (Hiện ngay lập tức từ list để mượt)
            AsyncImage(
                model = initialUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray)
            )

            // Logic hiển thị chi tiết (TimeAgo, User avatar) khi API load xong
            when (state) {
                is DetailState.Success -> {
                    val data = (state as DetailState.Success).data
                    val timeAgo = viewModel.calculateTimeAgo(data.uploadAt)

                    // Overlay thông tin
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(36.dp).background(Color(0xFF6650a4), CircleShape).border(2.dp, Color.Black, CircleShape)
                            ) {
                                Text(text = data.uploader.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = data.uploader.username,
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black, blurRadius = 10f))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeAgo,
                                color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp,
                                style = LocalTextStyle.current.copy(shadow = androidx.compose.ui.graphics.Shadow(color = Color.Black, blurRadius = 10f))
                            )
                        }
                    }
                }
                is DetailState.Error -> {
                    // Xử lý lỗi nhẹ nếu cần
                }
                else -> {
                    // Đang loading detail, nhưng ảnh đã hiện nhờ initialUrl
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. REACTION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(25.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Send message...", color = Color.Gray, fontSize = 14.sp,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            val emojis = listOf("💛", "🔥", "😍", "☺\uFE0F")
            emojis.forEach { emoji ->
                Text(
                    text = emoji, fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 4.dp).clickable { /* TODO */ }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 4. BOTTOM NAVIGATION ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Grid (Thay vì Back)
            IconButton(onClick = onSwitchToGrid) {
                Icon(Icons.Rounded.GridView, contentDescription = "Grid View", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            // Nút Camera (Về màn hình chụp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(60.dp).border(3.dp, LocketYellow, CircleShape).padding(4.dp).clip(CircleShape).background(Color.Gray)
                    .clickable { onNavigateToCamera() }
            ) {}
            // Nút Share
            IconButton(onClick = { }) {
                Icon(Icons.Rounded.IosShare, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}