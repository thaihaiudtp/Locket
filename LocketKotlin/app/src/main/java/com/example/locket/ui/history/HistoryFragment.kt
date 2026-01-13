package com.example.locket.ui.history

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager // Import này
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.locket.R
import com.example.locket.databinding.FragmentHistoryBinding
import com.example.locket.util.TimeUtils // Import TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels()
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var gridAdapter: HistoryGridAdapter
    private lateinit var detailAdapter: HistoryDetailAdapter

    private var isGridMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupAdapters()
        setupUI()
        observeViewModel()
        handleBackPress()
    }

    private fun setupAdapters() {
        // 1. Grid Adapter
        gridAdapter = HistoryGridAdapter { position ->
            // Khi click vào ảnh nhỏ: chuyển sang chế độ Detail và cuộn tới ảnh đó
            switchMode(isGrid = false)
            // Cần layoutManager để scrollToPosition hoạt động chính xác
            binding.rvDetail.scrollToPosition(position)
        }

        // 2. Detail Adapter
        detailAdapter = HistoryDetailAdapter(
            onSwitchToGrid = { switchMode(isGrid = true) },
            onBackToCamera = { findNavController().popBackStack() },
            onNavigateToProfile = {
                findNavController().navigate(R.id.action_historyFragment_to_profileFragment)
            },
            calculateTimeAgo = { timeStr ->
                // Sử dụng TimeUtils để tính toán
                TimeUtils.getTimeAgo(timeStr)
            }
        )
    }

    private fun setupUI() {
        // --- SỬA LỖI GRID VIEW ---
        // Thiết lập LayoutManager bằng code để chắc chắn nó hoạt động
        val gridLayoutManager = GridLayoutManager(context, 3)
        binding.rvGrid.layoutManager = gridLayoutManager
        binding.rvGrid.adapter = gridAdapter

        // --- SETUP DETAIL VIEW ---
        binding.rvDetail.adapter = detailAdapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvDetail)

        // Nút đóng Grid (dấu X hoặc mũi tên)
        binding.btnCloseGrid.setOnClickListener {
            switchMode(isGrid = false)
        }
        switchMode(isGridMode)
        // Load dữ liệu
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
                // Cập nhật dữ liệu cho cả 2 adapter
                android.util.Log.d("HistoryFragment", "Data size: ${pictures.size}")
                gridAdapter.submitList(pictures)
                detailAdapter.submitList(pictures)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Chỉ hiện loading nếu danh sách đang rỗng
                binding.progressBar.isVisible = isLoading && gridAdapter.itemCount == 0
            }
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isGridMode) {
                    // Nếu đang ở Grid -> Back về Detail
                    switchMode(isGrid = false)
                } else {
                    // Nếu đang ở Detail -> Back về Camera
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