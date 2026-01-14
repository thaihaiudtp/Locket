package com.example.locket.ui.history

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.locket.R
import com.example.locket.data.TokenManager // Import TokenManager
import com.example.locket.databinding.FragmentHistoryBinding
import com.example.locket.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels()
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // [MỚI] Inject TokenManager để lấy ID từ token đã lưu
    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var gridAdapter: HistoryGridAdapter
    private lateinit var detailAdapter: HistoryDetailAdapter

    private var isGridMode = false

    // Biến này sẽ được cập nhật từ TokenManager
    private var currentUserId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        // [QUAN TRỌNG] Lấy UserID trước khi setup UI
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = tokenManager.userIdFlow.first()

            if (!userId.isNullOrEmpty()) {
                currentUserId = userId
                Log.d("HistoryFragment", "Logged in as: $currentUserId")

                // Chỉ setup khi đã có ID để logic Reply (ẩn/hiện nút gửi) hoạt động đúng
                setupAdapters()
                setupUI()
                observeViewModel()
            } else {
                // Nếu không có token -> Về màn hình Login
                Toast.makeText(context, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_historyFragment_to_loginFragment) // Đảm bảo action này tồn tại trong nav_graph
            }
        }

        handleBackPress()
    }

    private fun setupAdapters() {
        // Grid Adapter
        gridAdapter = HistoryGridAdapter { position ->
            switchMode(isGrid = false)
            binding.rvDetail.scrollToPosition(position)
        }

        // Detail Adapter
        detailAdapter = HistoryDetailAdapter(
            currentUserId = currentUserId, // ID này giờ là ID thật
            onSwitchToGrid = { switchMode(isGrid = true) },
            onBackToCamera = { findNavController().popBackStack() },
            onNavigateToProfile = {
                findNavController().navigate(R.id.action_historyFragment_to_profileFragment)
            },
            onNavigateToChat = {
                try {
                    findNavController().navigate(R.id.action_historyFragment_to_conversationFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            calculateTimeAgo = { timeStr ->
                TimeUtils.getTimeAgo(timeStr)
            },
            onSendMessage = { receiverId, content, pictureId ->
                viewModel.sendMessage(receiverId, content, pictureId) {
                    Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupUI() {
        val gridLayoutManager = GridLayoutManager(context, 3)
        binding.rvGrid.layoutManager = gridLayoutManager
        binding.rvGrid.adapter = gridAdapter

        binding.rvDetail.adapter = detailAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvDetail)

        binding.btnCloseGrid.setOnClickListener {
            switchMode(isGrid = false)
        }

        // Mặc định vào là Grid hay Detail tuỳ bạn chỉnh biến isGridMode ban đầu
        switchMode(isGridMode)
        viewModel.loadNextPage()
    }

    private fun switchMode(isGrid: Boolean) {
        isGridMode = isGrid
        binding.layoutGridMode.isVisible = isGrid
        binding.rvDetail.isVisible = !isGrid
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pictures.collect { pictures ->
                gridAdapter.submitList(pictures)
                detailAdapter.submitList(pictures)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading && gridAdapter.itemCount == 0
            }
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isGridMode) {
                    switchMode(isGrid = false)
                } else {
                    findNavController().popBackStack()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}