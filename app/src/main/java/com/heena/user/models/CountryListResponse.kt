package com.heena.user.models

import java.io.Serializable

data class CountryListResponse(
	val country: List<Country?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable
