package com.heena.user.models

import java.io.Serializable

data class UserProfileResponse(
	val profile: Profile? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable

