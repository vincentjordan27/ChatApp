package com.vincent.chatapp.listeners

import com.vincent.chatapp.models.User

interface UserListener {
    fun onUserClicked(user: User)
}