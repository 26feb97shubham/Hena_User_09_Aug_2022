package com.heena.user.models

import java.io.Serializable

data class LoginResponse(
	val image: String? = null,
	val message: String? = null,
	val error: Error? = null,
	val email: String? = null,
	val status: Int? = null,
	val token: String? = null,
	val user : User? = null
): Serializable

