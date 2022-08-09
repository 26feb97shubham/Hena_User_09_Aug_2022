package com.heena.user.models

import java.io.Serializable

data class OTPResend(
    val message: String? = null,
    val status: Int? = null
): Serializable
