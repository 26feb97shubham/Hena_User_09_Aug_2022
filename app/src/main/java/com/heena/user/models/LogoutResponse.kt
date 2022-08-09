package com.heena.user.models

import java.io.Serializable

data class LogoutResponse(
	val message: String? = null,
	val error: Error? = null,
	val status: Int? = null
): Serializable

