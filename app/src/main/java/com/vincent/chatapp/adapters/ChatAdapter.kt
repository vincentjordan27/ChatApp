package com.vincent.chatapp.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vincent.chatapp.databinding.ItemContainerReceivedMessageBinding
import com.vincent.chatapp.databinding.ItemContainerSentMessageBinding
import com.vincent.chatapp.models.ChatMessage

class ChatAdapter( private val chatMessages: List<ChatMessage>, private val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var receiverProfileImage: Bitmap? = null

    fun setReceiverProfileImage(bitmap: Bitmap?) {
        receiverProfileImage = bitmap
    }
    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            with(binding) {
                textMessage.text = chatMessage.message
                textDateTime.text = chatMessage.dateTime
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap?) {
            with(binding) {
                textMessage.text = chatMessage.message
                textDateTime.text = chatMessage.dateTime
                Log.d("DEBUGS", receiverProfileImage.toString())
                if (receiverProfileImage != null) {
                    imageProfile.setImageBitmap(receiverProfileImage)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }else {
            ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        }else {
            (holder as ReceivedMessageViewHolder).setData(chatMessages[position], receiverProfileImage)
        }
    }


    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) {
            VIEW_TYPE_SENT
        }else {
            VIEW_TYPE_RECEIVED
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

}