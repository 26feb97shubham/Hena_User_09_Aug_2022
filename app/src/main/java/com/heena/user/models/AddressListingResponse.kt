package com.heena.user.models

import java.io.Serializable

data class AddressListingResponse(
	val address: List<AddressItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable

