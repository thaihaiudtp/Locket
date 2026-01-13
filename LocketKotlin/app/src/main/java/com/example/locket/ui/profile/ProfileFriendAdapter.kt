package com.example.locket.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.locket.databinding.ItemProfileFriendBinding
import com.example.locket.model.UserDTO

class ProfileFriendAdapter : RecyclerView.Adapter<ProfileFriendAdapter.ViewHolder>() {
    private var friends = listOf<UserDTO>()

    fun submitList(list: List<UserDTO>) {
        friends = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemProfileFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.binding.tvItemUsername.text = friend.username
        holder.binding.tvItemEmail.text = friend.email
        holder.binding.tvItemAvatar.text = friend.username.take(1).uppercase()
    }

    override fun getItemCount() = friends.size
}