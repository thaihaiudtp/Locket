package com.example.locket.ui.message

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locket.R
import com.example.locket.data.TokenManager
import com.example.locket.databinding.FragmentChatDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {

    private val viewModel: MessageViewModel by viewModels()
    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    // Inject TokenManager để lấy ID thật
    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var adapter: MessageAdapter
    private var conversationId: String? = null
    private var receiverId: String? = null
    private var myUserId: String = ""

    // Biến quản lý Job Polling để có thể dừng khi cần
    private var pollingJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatDetailBinding.bind(view)

        // Lấy tham số truyền sang
        conversationId = arguments?.getString("conversationId")
        receiverId = arguments?.getString("receiverId")

        // 1. Lấy UserID thật trước khi setup UI
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = tokenManager.userIdFlow.first()
            if (!userId.isNullOrEmpty()) {
                myUserId = userId
                setupUI()
            } else {
                Toast.makeText(context, "Lỗi xác thực", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupUI() {
        setupAdapter()
        setupActions()

        // Bắt đầu Polling (Fake Realtime)
        startPolling()

        // Observer dữ liệu tin nhắn
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { msgs ->
                // BE trả về mới nhất trước -> đảo ngược để hiển thị từ trên xuống dưới theo thời gian
                adapter.submitList(msgs.reversed()) {
                    // Sau khi list update xong thì cuộn xuống cuối
                    if (msgs.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(msgs.size - 1)
                    }
                }
            }
        }
    }

    private fun setupActions() {
        // 2. Xử lý nút Back
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 3. Xử lý gửi tin nhắn
        binding.btnSendChat.setOnClickListener {
            val content = binding.etChatInput.text.toString().trim()
            if (content.isNotBlank() && receiverId != null) {
                // Gửi tin nhắn
                viewModel.sendMessage(receiverId!!, content) {
                    // Callback onSuccess:
                    binding.etChatInput.setText("")

                    // Gọi refresh ngay lập tức (không cần chờ 2s của polling)
                    conversationId?.let { viewModel.loadMessages(it) }
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter = MessageAdapter(myUserId)
        binding.rvMessages.adapter = adapter
    }
    private fun startPolling() {
        // Hủy job cũ nếu có
        pollingJob?.cancel()

        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) { // Chạy vòng lặp miễn là Fragment còn sống
                conversationId?.let {
                    viewModel.loadMessages(it)
                }
                delay(2000) // Nghỉ 2 giây
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingJob?.cancel() // Dừng polling khi thoát màn hình
        _binding = null
    }
}