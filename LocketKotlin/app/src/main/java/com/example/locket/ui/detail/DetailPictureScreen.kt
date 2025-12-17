package com.example.locket.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.locket.ui.theme.LocketYellow // Đảm bảo import màu

@Composable
fun DetailPictureScreen(
    pictureId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit
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
        horizontalAlignment = Alignment.CenterHorizontally // Căn giữa tất cả theo chiều ngang
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

        // Spacer đẩy ảnh ra giữa
        Spacer(modifier = Modifier.weight(1f))

        // --- 2. MAIN CONTENT (HÌNH VUÔNG) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(1f) // <--- QUAN TRỌNG: Ép thành hình vuông
        ) {
            when (state) {
                is DetailState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LocketYellow)
                    }
                }
                is DetailState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((state as DetailState.Error).message, color = Color.Red)
                    }
                }
                is DetailState.Success -> {
                    val data = (state as DetailState.Success).data
                    val timeAgo = viewModel.calculateTimeAgo(data.uploadAt)

                    // Ảnh
                    AsyncImage(
                        model = data.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // Crop để lấp đầy hình vuông
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.DarkGray)
                    )

                    // Thông tin User (Đè lên ảnh)
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

        // Spacer đẩy Bottom Nav xuống đáy
        Spacer(modifier = Modifier.weight(1f))

        // --- 4. BOTTOM NAVIGATION ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Rounded.GridView, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(30.dp))
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