package com.vincent.chatapp.models

import java.util.*

data class ChatMessage(
    val senderId: String,
    val receiverId: String,
    var message: String,
    val dateTime: String = "",
    var dateObject: Long = 0,
    val conversionId: String = "",
    val conversionName: String = "",
    val conversionImage: String = "",
)
