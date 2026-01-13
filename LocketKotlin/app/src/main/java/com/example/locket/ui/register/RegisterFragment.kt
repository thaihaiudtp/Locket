package com.example.locket.ui.register

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locket.R
import com.example.locket.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: RegisterViewModel by viewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Xử lý nút Đăng ký
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Gọi ViewModel (đảm bảo RegisterViewModel có hàm register nhận 3 tham số này)
            viewModel.register(email, username, password)
        }

        // Xử lý nút chuyển về Login
        binding.btnNavigateLogin.setOnClickListener {
            // Quay lại màn hình trước đó (Login)
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is RegisterState.Loading -> {
                        binding.btnSignup.text = ""
                        binding.btnSignup.isEnabled = false
                        binding.progressBar.isVisible = true
                    }
                    is RegisterState.Success -> {
                        resetButtonState()
                        Toast.makeText(context, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show()
                        // Quay về trang Login sau khi đăng ký thành công
                        findNavController().popBackStack()
                    }
                    is RegisterState.Error -> {
                        resetButtonState()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is RegisterState.Idle -> {
                        resetButtonState()
                    }
                }
            }
        }
    }

    private fun resetButtonState() {
        binding.btnSignup.text = "Sign Up"
        binding.btnSignup.isEnabled = true
        binding.progressBar.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}