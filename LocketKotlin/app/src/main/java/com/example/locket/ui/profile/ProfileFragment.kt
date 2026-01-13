package com.example.locket.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locket.R
import com.example.locket.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val friendAdapter = ProfileFriendAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.rvFriends.adapter = friendAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.btnDeleteAccount.setOnClickListener {
            viewModel.deleteAccount()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update User Info
                binding.tvUsername.text = state.user?.username ?: "Loading..."
                binding.tvEmail.text = state.user?.email ?: ""
                binding.tvBigAvatar.text = state.user?.username?.take(1)?.uppercase() ?: "?"

                // Update Friend List
                binding.tvFriendsTitle.text = "Friends (${state.friends.size})"
                friendAdapter.submitList(state.friends)

                // Handle Logout
                if (state.isLoggedOut) {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    // Điều hướng về Login và xóa backstack
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }

                // Handle Error (Optional)
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}