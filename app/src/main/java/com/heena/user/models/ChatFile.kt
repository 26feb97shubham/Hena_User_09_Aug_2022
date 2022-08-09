package com.heena.user.models

import java.io.Serializable

data class ChatFile(
    val chat_file: String,
    val mime_type: String,
    val url: String
): Serializable