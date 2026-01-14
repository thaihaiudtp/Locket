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
import com.example.locket.data.TokenManager
import com.example.locket.databinding.FragmentConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

    private val viewModel: MessageViewModel by viewModels()
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: TokenManager

    private var currentUserId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConversationBinding.bind(view)

        // [MỚI] Xử lý sự kiện nút Back ngay khi View được tạo
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = tokenManager.userIdFlow.first()
            if (!userId.isNullOrEmpty()) {
                currentUserId = userId
                setupUI()
            } else {
                Toast.makeText(context, "Lỗi xác thực, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupUI() {
        val adapter = ConversationAdapter(currentUserId) { conversation, receiverId, receiverName ->
            val bundle = Bundle().apply {
                putString("conversationId", conversation.id)
                putString("receiverId", receiverId)
            }
            findNavController().navigate(R.id.action_conversation_to_detail, bundle)
        }

        binding.rvConversations.adapter = adapter
        binding.rvConversations.layoutManager = LinearLayoutManager(context)

        viewModel.loadConversations()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversations.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}