package com.example.locket.ui.friend

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.locket.model.FriendRequestReceivedItem
import com.example.locket.model.FriendRequestSentItem
import com.example.locket.model.UserDTO
import com.example.locket.ui.theme.LocketYellow

@Composable
fun FriendScreen(
    viewModel: FriendViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Show error toast
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
    ) {
        // --- Header ---
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.background(Color.DarkGray, CircleShape).size(40.dp)
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Add new friends",
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // --- Search Bar ---
        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.searchUser(it)
            },
            placeholder = { Text("Add a new friend", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = ""; viewModel.searchUser("") }) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = LocketYellow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Loading ---
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocketYellow)
            }
        }

        // --- Content List ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Phần 1: Kết quả tìm kiếm (Chỉ hiện khi đang search)
            if (searchText.isNotEmpty()) {
                if (state.searchResults.isEmpty() && !state.isLoading) {
                    item {
                        Text("No users found", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    items(state.searchResults) { user ->
                        UserSearchItem(
                            user = user,
                            onAddClick = { viewModel.sendFriendRequest(user) }
                        )
                    }
                }
            } else {
                // Phần 2: Lời mời ĐÃ NHẬN (Received)
                if (state.receivedRequests.isNotEmpty()) {
                    item {
                        SectionTitle("Added me")
                    }
                    items(state.receivedRequests) { req ->
                        FriendRequestItem(
                            user = req.sender,
                            isReceived = true,
                            onAccept = { viewModel.acceptRequest(req.id) },
                            onDelete = { viewModel.rejectRequest(req.id) }
                        )
                    }
                }

                // Phần 3: Lời mời ĐÃ GỬI (Sent)
                if (state.sentRequests.isNotEmpty()) {
                    item {
                        SectionTitle("Added by me (Pending)")
                    }
                    items(state.sentRequests) { req ->
                        // Lưu ý: với sent request, 'receiver' là người mình gửi tới
                        FriendRequestItem(
                            user = req.receiver,
                            isReceived = false,
                            onAccept = {},
                            onDelete = { viewModel.rejectRequest(req.id) } // Hủy lời mời đã gửi
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = LocketYellow,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Item cho kết quả tìm kiếm
@Composable
fun UserSearchItem(user: UserDTO, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UserInfoRow(username = user.username, email = user.email)

        // Nút Add / Status
        val status = user.relationshipStatus ?: "none"
        if (status == "none") {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = LocketYellow),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            // Hiển thị trạng thái nếu đã gửi/là bạn bè
            val statusText = when (status) {
                "sent" -> "Sent"
                "received" -> "Accept" // Có thể cho nút accept ở đây nếu muốn
                "friend" -> "Friend"
                else -> ""
            }
            Text(text = statusText, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

// Item cho Friend Request (Sent/Received)
@Composable
fun FriendRequestItem(
    user: UserDTO,
    isReceived: Boolean,
    onAccept: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UserInfoRow(username = user.username, email = user.email)

        Row {
            if (isReceived) {
                // Nút Accept (chỉ hiện cho Received)
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = LocketYellow),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Nút Xóa/Hủy
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).background(Color.DarkGray, CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun UserInfoRow(username: String, email: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(50.dp).background(Color.Gray, CircleShape)
        ) {
            Text(
                text = username.take(1).uppercase(),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = email, color = Color.Gray, fontSize = 12.sp)
        }
    }
}