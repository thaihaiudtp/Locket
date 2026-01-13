package com.example.locket.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locket.R
import com.example.locket.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Nút Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        // Chuyển sang màn hình Register
        binding.btnNavigateRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Loading -> {
                        binding.btnLogin.text = "" // Ẩn chữ để hiện loading
                        binding.btnLogin.isEnabled = false
                        binding.progressBar.isVisible = true
                    }
                    is LoginState.Success -> {
                        resetButtonState()
                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        // Điều hướng sang Camera (xóa backstack Login)
                        findNavController().navigate(R.id.action_loginFragment_to_cameraFragment)
                    }
                    is LoginState.Error -> {
                        resetButtonState()
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Idle -> {
                        resetButtonState()
                    }
                }
            }
        }
    }

    private fun resetButtonState() {
        binding.btnLogin.text = "Log In"
        binding.btnLogin.isEnabled = true
        binding.progressBar.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}