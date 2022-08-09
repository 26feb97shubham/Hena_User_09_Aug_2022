package com.heena.user.models

import java.io.Serializable

data class FavManagerListingResponse(
	val manager: List<Any?>? = null,
	val service: List<Service?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable


