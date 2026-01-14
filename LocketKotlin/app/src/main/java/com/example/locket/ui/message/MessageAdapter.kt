package com.example.locket.ui.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.locket.databinding.ItemMessageBinding
import com.example.locket.model.MessageDTO

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<MessageDTO, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessageDTO) {
            val isMe = item.sender == currentUserId

            // Lấy URL ảnh: ưu tiên lấy từ attachmentUrl (root) hoặc attachedPicture.url (object)
            val imageUrl = item.attachmentUrl ?: item.attachedPicture?.url

            if (isMe) {
                // --- TIN NHẮN CỦA MÌNH (BÊN PHẢI) ---
                binding.layoutMe.isVisible = true
                binding.layoutOther.isVisible = false

                // 1. Xử lý Text
                if (item.content.isNotBlank()) {
                    binding.tvMsgMe.isVisible = true
                    binding.tvMsgMe.text = item.content
                } else {
                    binding.tvMsgMe.isVisible = false
                }

                // 2. Xử lý Ảnh (Ẩn/Hiện CardView chứa ảnh)
                if (!imageUrl.isNullOrEmpty()) {
                    binding.cardImgMe.isVisible = true
                    binding.imgAttachMe.isVisible = true
                    binding.imgAttachMe.load(imageUrl) {
                        crossfade(true)
                        // Không cần transformation nữa vì CardView đã bo góc
                    }
                } else {
                    binding.cardImgMe.isVisible = false
                }

            } else {
                // --- TIN NHẮN NGƯỜI KHÁC (BÊN TRÁI) ---
                binding.layoutMe.isVisible = false
                binding.layoutOther.isVisible = true

                // 1. Xử lý Text
                if (item.content.isNotBlank()) {
                    binding.tvMsgOther.isVisible = true
                    binding.tvMsgOther.text = item.content
                } else {
                    binding.tvMsgOther.isVisible = false
                }

                // 2. Xử lý Ảnh (Ẩn/Hiện CardView chứa ảnh)
                if (!imageUrl.isNullOrEmpty()) {
                    binding.cardImgOther.isVisible = true
                    binding.imgAttachOther.isVisible = true
                    binding.imgAttachOther.load(imageUrl) {
                        crossfade(true)
                    }
                } else {
                    binding.cardImgOther.isVisible = false
                }
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<MessageDTO>() {
    override fun areItemsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
        return oldItem == newItem
    }
}