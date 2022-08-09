package com.heena.user.models

import java.io.Serializable

data class BookingDetailsResponse(
	val booking: Booking? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable


