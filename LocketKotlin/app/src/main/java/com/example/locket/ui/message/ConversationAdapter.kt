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
    private val currentUserId: String, // ID của người dùng hiện tại (để biết ai là người đối diện)
    private val onItemClick: (ConversationDTO) -> Unit
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
            // Logic: Tìm ID người kia trong danh sách participants
            val otherUserId = item.participants.firstOrNull { it != currentUserId } ?: "Unknown"

            // TODO: Ở đây bạn nên dùng ID này để lấy tên User từ Cache hoặc gọi API phụ
            // Tạm thời hiển thị ID rút gọn
            binding.tvUsername.text = "User: ${otherUserId.take(5)}..."
            binding.tvAvatar.text = otherUserId.take(1).uppercase()

            // Hiển thị thời gian tin nhắn cuối
            binding.tvLastMsg.text = formatTime(item.lastMessageAt)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun formatTime(timeStr: String?): String {
            if (timeStr.isNullOrEmpty()) return ""
            return try {
                // Giả sử server trả về ISO 8601
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
    override fun areItemsTheSame(oldItem: ConversationDTO, newItem: ConversationDTO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ConversationDTO, newItem: ConversationDTO): Boolean {
        return oldItem == newItem
    }
}