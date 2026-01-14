package com.example.locket.ui.message

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locket.R
import com.example.locket.databinding.FragmentConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val viewModel: MessageViewModel by viewModels()
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    // TODO: Bạn cần thay thế giá trị này bằng ID thật của user đang đăng nhập
    // (Lấy từ SharedPreferences, DataStore hoặc UserSession nơi bạn lưu token)
    private val currentUserId = "ID_CUA_MINH_LOG_TU_TOKEN"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConversationBinding.bind(view)

        setupUI()
    }

    private fun setupUI() {
        // Khởi tạo Adapter: Truyền currentUserId vào để Adapter biết đâu là mình, đâu là bạn
        val adapter = ConversationAdapter(currentUserId) { conversation ->
            // === LOGIC LẤY ID ĐỐI PHƯƠNG ===
            // participants là danh sách [ID_CUA_MINH, ID_DOI_PHUONG]
            // Ta tìm cái ID nào KHÁC currentUserId thì đó là người kia
            val receiverId = conversation.participants.firstOrNull { it != currentUserId }

            if (receiverId != null) {
                val bundle = Bundle().apply {
                    putString("conversationId", conversation.id)
                    putString("receiverId", receiverId)
                }
                // Điều hướng sang màn hình chat chi tiết
                // Đảm bảo bạn đã tạo action này trong nav_graph.xml
                findNavController().navigate(R.id.action_conversation_to_detail, bundle)
            } else {
                Toast.makeText(context, "Không xác định được người nhận", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvConversations.adapter = adapter
        binding.rvConversations.layoutManager = LinearLayoutManager(context)

        // Gọi API tải danh sách hội thoại
        viewModel.loadConversations()

        // Observer: Lắng nghe dữ liệu trả về từ ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversations.collect { list ->
                // Kiểm tra nếu list rỗng thì có thể hiện text "Chưa có tin nhắn" (tuỳ chọn)
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}