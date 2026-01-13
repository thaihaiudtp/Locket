package com.example.locket.ui.friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.locket.databinding.ItemFriendActionBinding
import com.example.locket.databinding.ItemSectionHeaderBinding
import com.example.locket.model.UserDTO

// Định nghĩa các loại item
sealed class FriendItem {
    data class Header(val title: String) : FriendItem()
    data class UserSearch(val user: UserDTO) : FriendItem()
    data class RequestReceived(val id: String, val sender: UserDTO) : FriendItem()
    data class RequestSent(val id: String, val receiver: UserDTO) : FriendItem()
}

class FriendAdapter(
    private val onAddClick: (UserDTO) -> Unit,
    private val onAcceptClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<FriendItem>()

    fun submitList(newItems: List<FriendItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FriendItem.Header -> 0
            else -> 1 // Các loại User item dùng chung layout action
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            HeaderViewHolder(ItemSectionHeaderBinding.inflate(inflater, parent, false))
        } else {
            ActionViewHolder(ItemFriendActionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FriendItem.Header -> (holder as HeaderViewHolder).bind(item)

            // Ép kiểu cho cả 3 trường hợp dùng ActionViewHolder
            is FriendItem.UserSearch -> (holder as ActionViewHolder).bindSearch(item.user)
            is FriendItem.RequestReceived -> (holder as ActionViewHolder).bindReceived(item)
            is FriendItem.RequestSent -> (holder as ActionViewHolder).bindSent(item)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FriendItem.Header) {
            binding.tvSectionTitle.text = item.title
        }
    }

    inner class ActionViewHolder(private val binding: ItemFriendActionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindSearch(user: UserDTO) {
            bindUserBasic(user)
            resetButtons()

            val status = user.relationshipStatus ?: "none"
            if (status == "none") {
                binding.btnAdd.isVisible = true
                binding.btnAdd.setOnClickListener { onAddClick(user) }
            } else {
                binding.tvStatus.isVisible = true
                binding.tvStatus.text = when (status) {
                    "sent" -> "Sent"
                    "friend" -> "Friend"
                    "received" -> "Accept"
                    else -> ""
                }
            }
        }

        fun bindReceived(item: FriendItem.RequestReceived) {
            bindUserBasic(item.sender)
            resetButtons()

            binding.btnAccept.isVisible = true
            binding.btnAccept.setOnClickListener { onAcceptClick(item.id) }

            binding.btnDelete.isVisible = true
            binding.btnDelete.setOnClickListener { onDeleteClick(item.id) }
        }

        fun bindSent(item: FriendItem.RequestSent) {
            bindUserBasic(item.receiver)
            resetButtons()

            binding.btnDelete.isVisible = true // Nút hủy lời mời
            binding.btnDelete.setOnClickListener { onDeleteClick(item.id) }
        }

        private fun bindUserBasic(user: UserDTO) {
            binding.tvItemUsername.text = user.username
            binding.tvItemEmail.text = user.email
            binding.tvItemAvatar.text = user.username.take(1).uppercase()
        }

        private fun resetButtons() {
            binding.btnAdd.isVisible = false
            binding.btnAccept.isVisible = false
            binding.btnDelete.isVisible = false
            binding.tvStatus.isVisible = false
        }
    }
}