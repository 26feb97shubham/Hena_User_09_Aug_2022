package com.heena.user.models

import java.io.Serializable

data class OldMessagesResponse(
    val message: String,
    val messages: List<Message>,
    val status: Int
): Serializable