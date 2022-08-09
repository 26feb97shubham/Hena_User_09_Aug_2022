package com.heena.user.models

import java.io.Serializable

data class Message(
    val big_room: Int,
    val created_at: String,
    val date_time: String,
    val help_id: Int,
    val id: Int,
    val image: String,
    val url: String,
    val is_read: Int,
    val message: String,
    val message_type: String,
    val mime_type: String,
    val name: String,
    val receiver_id: Int,
    val room: String,
    val sender_id: Int,
    val small_room: Int,
    val sub_help_id: Int,
    val updated_at: String
): Serializable