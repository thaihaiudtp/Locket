package com.example.locket.ui.message

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.ConversationDTO
import com.example.locket.model.MessageDTO
import com.example.locket.model.SendMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel() {

    // Danh sách hội thoại
    private val _conversations = MutableStateFlow<List<ConversationDTO>>(emptyList())
    val conversations = _conversations.asStateFlow()

    // Danh sách tin nhắn của hội thoại đang mở
    private val _messages = MutableStateFlow<List<MessageDTO>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 1. Lấy danh sách hội thoại
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getConversations()
                if (response.isSuccessful && response.body()?.success == true) {
                    _conversations.value = response.body()!!.data
                } else {
                    Log.e("MessageVM", "Load conv fail: ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 2. Lấy tin nhắn chi tiết
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getMessagesInConversation(conversationId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // API trả về mờ nhất trước -> Cần đảo ngược để hiển thị từ dưới lên (nếu dùng reverseLayout)
                    // Hoặc giữ nguyên tuỳ cách setup RecyclerView.
                    // Ở code BE: .sort((a, b) => b.createdAt - a.createdAt) -> Mới nhất ở đầu mảng
                    _messages.value = response.body()!!.data.messages
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 3. Gửi tin nhắn
    fun sendMessage(receiverId: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val req = SendMessageRequest(receiverId, content)
                val response = apiService.sendMessage(req)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                    // Reload lại tin nhắn sau khi gửi (hoặc tự append vào list để mượt hơn)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Hàm tiện ích để lấy ID của người đối diện trong cuộc trò chuyện
    // (Vì BE trả về list participants gồm cả ID của mình)
    fun getOtherUserId(participants: List<String>, myUserId: String): String {
        return participants.firstOrNull { it != myUserId } ?: ""
    }
}