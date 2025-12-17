package com.example.locket.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
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

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackToCamera: () -> Unit,
    // 1. THÊM THAM SỐ NÀY (Hàm callback khi click vào ảnh)
    onPictureClick: (String) -> Unit
) {
    val pictures by viewModel.pictures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val gridState = rememberLazyGridState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- Custom Top Bar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onBackToCamera,
                modifier = Modifier.align(Alignment.CenterStart)
                    .background(Color.DarkGray, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.DarkGray, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Everyone",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // --- Grid Ảnh ---
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
                    LaunchedEffect(Unit) {
                        viewModel.loadNextPage()
                    }
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                        .clickable {
                            // 2. GỌI HÀM CALLBACK THAY VÌ NAVCONTROLLER
                            onPictureClick(picture.id)
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(30.dp))
            }
        }
    }
}