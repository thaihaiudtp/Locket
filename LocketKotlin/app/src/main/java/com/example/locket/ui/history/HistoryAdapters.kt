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
        // Load ảnh vào ô Grid
        holder.binding.imgGrid.load(item.url) {
            crossfade(true)
        }

        // Sự kiện Click
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    class GridViewHolder(val binding: ItemHistoryGridBinding) : RecyclerView.ViewHolder(binding.root)
}

// --- DETAIL ADAPTER ---
class HistoryDetailAdapter(
    private val onSwitchToGrid: () -> Unit,
    private val onBackToCamera: () -> Unit,
    private val onNavigateToProfile: () -> Unit,
    private val calculateTimeAgo: (String) -> String
) : ListAdapter<PictureData, HistoryDetailAdapter.DetailViewHolder>(PictureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemHistoryDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            imgMain.load(item.url)

            // Xử lý Time Stamp
            val timeString = calculateTimeAgo(item.uploadAt) // Truyền thời gian từ model vào
            tvTimeAgo.text = timeString
            tvTimeOverlay.text = timeString // Nếu bạn muốn overlay cũng hiện giống vậy

            // Các thông tin khác
            tvUsername.text = item.uploader.username
            tvAvatar.text = item.uploader.username.take(1).uppercase()

            // Ẩn hiện overlay nếu không có dữ liệu
            tvLocation.text = item.location ?: ""
            layoutLocation.isVisible = !item.location.isNullOrBlank()

            tvMessageOverlay.text = item.message
            tvMessageOverlay.isVisible = !item.message.isNullOrBlank()

            // Click Events
            btnSwitchToGrid.setOnClickListener { onSwitchToGrid() }
            btnBackToCamera.setOnClickListener { onBackToCamera() }
            btnProfile.setOnClickListener { onNavigateToProfile() }
        }
    }

    class DetailViewHolder(val binding: ItemHistoryDetailBinding) : RecyclerView.ViewHolder(binding.root)
}

class PictureDiffCallback : DiffUtil.ItemCallback<PictureData>() {
    override fun areItemsTheSame(oldItem: PictureData, newItem: PictureData) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: PictureData, newItem: PictureData) = oldItem == newItem
}