package com.heena.user.models

import java.io.Serializable

data class ChatFileUploadResponse(
    val chat_file: ChatFile,
    val message: String,
    val status: Int
): Serializable