package com.heena.user.models

import java.io.Serializable

data class Response(
    val code: String,
    val message: String
): Serializable