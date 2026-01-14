package com.example.locket.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.locket.databinding.ItemConversationBinding
import com.example.locket.model.ConversationDTO
import java.text.SimpleDateFormat
import java.util.Locale

class ConversationAdapter(
    private val currentUserId: String, // ID của mình
    private val onItemClick: (ConversationDTO, String, String) -> Unit // Trả về (Conversation, ReceiverId, ReceiverName)
) : ListAdapter<ConversationDTO, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConversationDTO) {
            // LOGIC MỚI: Tìm người kia trong danh sách object participants
            // Lấy người có id KHÁC currentUserId
            val otherParticipant = item.participants.firstOrNull { it.id != currentUserId }

            // Nếu chat với chính mình (participants gồm 2 người giống nhau) thì lấy người đầu tiên
            val targetUser = otherParticipant ?: item.participants.firstOrNull()

            if (targetUser != null) {
                binding.tvUsername.text = targetUser.username
                binding.tvAvatar.text = targetUser.username.take(1).uppercase()
            } else {
                binding.tvUsername.text = "Unknown"
                binding.tvAvatar.text = "?"
            }

            binding.tvLastMsg.text = formatTime(item.lastMessageAt)

            binding.root.setOnClickListener {
                if (targetUser != null) {
                    onItemClick(item, targetUser.id, targetUser.username)
                }
            }
        }

        private fun formatTime(timeStr: String?): String {
            if (timeStr.isNullOrEmpty()) return ""
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                val outputFormat = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                val date = inputFormat.parse(timeStr)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                "Just now"
            }
        }
    }
}

class ConversationDiffCallback : DiffUtil.ItemCallback<ConversationDTO>() {
    override fun areItemsTheSame(oldItem: ConversationDTO, newItem: ConversationDTO) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ConversationDTO, newItem: ConversationDTO) = oldItem == newItem
}