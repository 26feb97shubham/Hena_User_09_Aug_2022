package com.heena.user.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookingListingResponse(
	val booking: List<BookingItem?>? = null,
	val message: String? = null,
	val status: Int? = null,
): Serializable

