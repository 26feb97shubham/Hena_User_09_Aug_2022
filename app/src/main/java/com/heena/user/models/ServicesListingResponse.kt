package com.heena.user.models

import java.io.Serializable

data class ServicesListingResponse(
		val status: Int? = null,
		val message: String? = null,
	val service: List<ServiceItem?>? = null
): Serializable