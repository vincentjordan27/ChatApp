package com.vincent.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vincent.chatapp.databinding.ItemContainerUserBinding
import com.vincent.chatapp.listeners.UserListener
import com.vincent.chatapp.models.User

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    var onUserClicked : ((User) -> Unit)? = null
    inner class UserViewHolder(private val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setUserData(user: User) {
            with(binding) {
                textName.text = user.name
                textEmail.text = user.email
                imageProfile.setImageBitmap(getUserImage(user.image!!))
                root.setOnClickListener {
                    onUserClicked?.invoke(users[adapterPosition])
                }
            }
        }
    }

    private fun getUserImage(encodedImage: String) : Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }
}