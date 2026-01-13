package com.example.locket.ui.friend

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locket.R
import com.example.locket.databinding.FragmentFriendBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendFragment : Fragment(R.layout.fragment_friend) {

    private val viewModel: FriendViewModel by viewModels()
    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendBinding.bind(view)

        setupAdapter()
        setupUI()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = FriendAdapter(
            onAddClick = { user -> viewModel.sendFriendRequest(user) },
            onAcceptClick = { id -> viewModel.acceptRequest(id) },
            onDeleteClick = { id -> viewModel.rejectRequest(id) }
        )
        binding.rvFriends.adapter = adapter
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                binding.btnClearSearch.isVisible = query.isNotEmpty()
                viewModel.searchUser(query)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading

                // Handle Error Toast
                if (state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }

                // Build List Items
                val items = mutableListOf<FriendItem>()
                val searchText = binding.etSearch.text.toString()

                if (searchText.isNotEmpty()) {
                    // Search Mode
                    items.addAll(state.searchResults.map { FriendItem.UserSearch(it) })
                } else {
                    // Friend Requests Mode
                    if (state.receivedRequests.isNotEmpty()) {
                        items.add(FriendItem.Header("Added me"))
                        items.addAll(state.receivedRequests.map { FriendItem.RequestReceived(it.id, it.sender) })
                    }

                    if (state.sentRequests.isNotEmpty()) {
                        items.add(FriendItem.Header("Added by me (Pending)"))
                        items.addAll(state.sentRequests.map { FriendItem.RequestSent(it.id, it.receiver) })
                    }
                }

                adapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}