package com.example.locket.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.locket.R
import com.example.locket.databinding.FragmentDetailPictureBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailPictureFragment : Fragment(R.layout.fragment_detail_picture) {

    private val viewModel: DetailViewModel by viewModels()
    private var _binding: FragmentDetailPictureBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailPictureBinding.bind(view)

        // Lấy pictureId từ arguments (Truyền khi navigate)
        val pictureId = arguments?.getString("pictureId")

        if (pictureId != null) {
            viewModel.loadPictureDetail(pictureId)
        } else {
            Toast.makeText(context, "Error: Picture not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Nút Back (Góc trái dưới)
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Nút tròn to ở giữa -> Về Camera
        binding.btnBackToCamera.setOnClickListener {
            // Cách đơn giản nhất là pop về CameraFragment nếu nó có trong stack
            // Hoặc navigate action cụ thể
            findNavController().popBackStack(R.id.cameraFragment, false)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detailState.collect { state ->
                when (state) {
                    is DetailState.Loading -> {
                        binding.progressBar.isVisible = true
                    }
                    is DetailState.Success -> {
                        binding.progressBar.isVisible = false
                        val data = state.data

                        // Load ảnh
                        binding.imgMain.load(data.url)

                        // Set thông tin User
                        binding.tvUsername.text = data.uploader.username
                        binding.tvAvatar.text = data.uploader.username.take(1).uppercase()

                        // Tính thời gian (Sử dụng hàm tiện ích trong ViewModel)
                        binding.tvTimeAgo.text = viewModel.calculateTimeAgo(data.uploadAt)
                    }
                    is DetailState.Error -> {
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}