package com.heena.user.models

import java.io.Serializable

data class ManagerProfileResponse(
		val status: Int? = null,
		val offer: List<OfferItem>? = null,
		val service: List<ServiceItem>? = null,
		val message: String? = null
): Serializable