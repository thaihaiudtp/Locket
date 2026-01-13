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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.style.TextAlign
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
    onBackToCamera: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val pictures by viewModel.pictures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isGridMode by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { pictures.size })
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    BackHandler {
        if (isGridMode) isGridMode = false else onBackToCamera()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isGridMode) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All Memories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(
                        onClick = { isGridMode = false },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(pictures) { index, picture ->
                        if (index == pictures.lastIndex && !isLoading) {
                            LaunchedEffect(Unit) { viewModel.loadNextPage() }
                        }
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                                .clickable {
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
            if (pictures.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No photos yet", color = Color.Gray)
                    Button(onClick = onBackToCamera, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Take a photo")
                    }
                }
            } else {
                VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val pictureData = pictures.getOrNull(page)
                    if (pictureData != null) {
                        key(pictureData.id) {
                            HistoryDetailItem(
                                pictureId = pictureData.id,
                                initialUrl = pictureData.url,
                                initialUsername = pictureData.uploader.username,
                                message = pictureData.message,
                                time = pictureData.time,
                                location = pictureData.location,
                                onSwitchToGrid = { isGridMode = true },
                                onNavigateToCamera = onBackToCamera,
                                onNavigateToProfile = onNavigateToProfile
                            )
                        }
                    }
                }
            }
        }

        if (isLoading && pictures.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocketYellow)
            }
        }
    }
}

@Composable
fun HistoryDetailItem(
    pictureId: String,
    initialUrl: String,
    initialUsername: String,
    message: String?,
    time: String?,
    location: String?,
    onSwitchToGrid: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsState()

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
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(Color.DarkGray, CircleShape).clickable { onNavigateToProfile() }) {
                Icon(Icons.Rounded.Person, null, tint = Color.White)
            }
            Box(modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Everyone",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).background(Color.DarkGray, CircleShape)) {
                Icon(Icons.Rounded.ChatBubble, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(1f) // Hình vuông
        ) {
            AsyncImage(
                model = initialUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.DarkGray)
            )
            if (!time.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = time, color = LocketYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            if (!location.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = LocketYellow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = location, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            if (!message.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp) // Cách đáy ảnh một chút
                        .padding(horizontal = 32.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF6650a4), CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            ) {
                Text(
                    text = initialUsername.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = initialUsername,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = " • ",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            val displayTime = if (state is DetailState.Success) {
                viewModel.calculateTimeAgo((state as DetailState.Success).data.uploadAt)
            } else {
                "..."
            }

            Text(
                text = displayTime,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.weight(1f))
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

        // --- 5. BOTTOM NAVIGATION ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSwitchToGrid) {
                Icon(Icons.Rounded.GridView, contentDescription = "Grid View", tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(60.dp).border(3.dp, LocketYellow, CircleShape).padding(4.dp).clip(CircleShape).background(Color.Gray)
                    .clickable { onNavigateToCamera() }
            ) {}
            IconButton(onClick = { }) {
                Icon(Icons.Rounded.IosShare, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}