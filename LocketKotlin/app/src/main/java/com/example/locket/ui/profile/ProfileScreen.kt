package com.example.locket.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.locket.ui.friend.UserInfoRow
import com.example.locket.ui.theme.LocketYellow

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Lắng nghe sự kiện logout thành công để chuyển màn hình
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
    ) {
        // --- HEADER ---
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(Color.DarkGray, CircleShape)
                    .size(40.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Profile",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- USER INFO ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar to
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, CircleShape)
            ) {
                Text(
                    // Lấy chữ cái đầu của tên (Nếu chưa load đc thì để ?)
                    text = state.user?.username?.take(1)?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.user?.username ?: "Loading...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Text(
                text = state.user?.email ?: "",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- FRIEND LIST ---
        Text("Friends (${state.friends.size})", color = LocketYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.friends) { friend ->
                // Tái sử dụng UserInfoRow từ FriendScreen (hoặc copy lại code đó)
                UserInfoRow(username = friend.username, email = friend.email)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ACTIONS (LOGOUT & DELETE) ---
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Logout Button
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Icon(Icons.Rounded.Logout, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Delete Account Button
            Button(
                onClick = { viewModel.deleteAccount() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E1E1E)) // Màu đỏ tối
            ) {
                Icon(Icons.Rounded.PersonRemove, null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}