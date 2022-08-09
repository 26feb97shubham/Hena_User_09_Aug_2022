package com.heena.user.models

import java.io.Serializable

data class ShowAddressResponse(
	val bank: Bank? = null,
	val address: Address? = null,
	val message: String? = null,
	val status: Int? = null
):Serializable



