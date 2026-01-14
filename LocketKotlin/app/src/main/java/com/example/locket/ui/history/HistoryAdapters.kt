package com.example.locket.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.locket.databinding.ItemHistoryDetailBinding
import com.example.locket.databinding.ItemHistoryGridBinding
import com.example.locket.model.PictureData

// --- GRID ADAPTER ---
class HistoryGridAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<PictureData, HistoryGridAdapter.GridViewHolder>(PictureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemHistoryGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.imgGrid.load(item.url) {
            crossfade(true)
        }
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    class GridViewHolder(val binding: ItemHistoryGridBinding) : RecyclerView.ViewHolder(binding.root)
}

// --- DETAIL ADAPTER ---
class HistoryDetailAdapter(
    private val currentUserId: String,
    private val onSwitchToGrid: () -> Unit,
    private val onBackToCamera: () -> Unit,
    private val onNavigateToProfile: () -> Unit,
    private val onNavigateToChat: () -> Unit, // [MỚI] Callback chuyển màn hình chat
    private val calculateTimeAgo: (String?) -> String,
    private val onSendMessage: (String, String, String) -> Unit
) : ListAdapter<PictureData, HistoryDetailAdapter.DetailViewHolder>(PictureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemHistoryDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            imgMain.load(item.url)

            val timeString = calculateTimeAgo(item.uploadAt)
            tvTimeAgo.text = timeString
            tvTimeOverlay.text = timeString

            tvUsername.text = item.uploader.username
            tvAvatar.text = item.uploader.username.take(1).uppercase()

            tvLocation.text = item.location ?: ""
            layoutLocation.isVisible = !item.location.isNullOrBlank()

            tvMessageOverlay.text = item.message
            tvMessageOverlay.isVisible = !item.message.isNullOrBlank()

            // Logic Reply Box
            if (item.uploader.id != currentUserId) {
                layoutReply.isVisible = true
                btnSendReply.setOnClickListener {
                    val msg = etReply.text.toString().trim()
                    if (msg.isNotEmpty()) {
                        onSendMessage(item.uploader.id, msg, item.id)
                        etReply.setText("")
                    }
                }
            } else {
                layoutReply.isVisible = false
            }

            // --- CLICK EVENTS ---
            btnSwitchToGrid.setOnClickListener { onSwitchToGrid() }
            btnBackToCamera.setOnClickListener { onBackToCamera() }
            btnProfile.setOnClickListener { onNavigateToProfile() }

            // [MỚI] Bắt sự kiện nút Chat ở Header
            btnChat.setOnClickListener { onNavigateToChat() }
        }
    }

    class DetailViewHolder(val binding: ItemHistoryDetailBinding) : RecyclerView.ViewHolder(binding.root)
}

class PictureDiffCallback : DiffUtil.ItemCallback<PictureData>() {
    override fun areItemsTheSame(oldItem: PictureData, newItem: PictureData) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: PictureData, newItem: PictureData) = oldItem == newItem
}