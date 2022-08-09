package com.heena.user.models

import java.io.Serializable

data class FavoriteServicesListingResponse(
	val service: List<ServiceItemY?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable

