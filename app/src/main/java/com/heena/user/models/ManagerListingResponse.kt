package com.heena.user.models

import java.io.Serializable

data class ManagerListingResponse(
	val manager: List<ManagerItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable

