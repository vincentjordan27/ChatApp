package com.vincent.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vincent.chatapp.databinding.ItemContainerRecentConversionBinding
import com.vincent.chatapp.models.ChatMessage
import com.vincent.chatapp.models.User

class RecentConversationAdapter(private val chatMessages: List<ChatMessage>) : RecyclerView.Adapter<RecentConversationAdapter.ViewHolder>() {

    var onConversionClicked : ((User) -> Unit)? = null
    inner class ViewHolder (private val binding: ItemContainerRecentConversionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user = User(
                    id = chatMessage.conversionId,
                    image = chatMessage.conversionImage,
                    name = chatMessage.conversionName
                )
                onConversionClicked?.invoke(user)
            }
        }
    }

     private fun getConversionImage(encodedImage: String) : Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentConversationAdapter.ViewHolder {
        return ViewHolder(ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecentConversationAdapter.ViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int = chatMessages.size

}