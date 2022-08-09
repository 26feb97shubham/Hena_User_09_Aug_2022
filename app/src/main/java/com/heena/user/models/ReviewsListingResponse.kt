package com.heena.user.models

import java.io.Serializable

data class ReviewsListingResponse(
	val comments: List<CommentsItem?>? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable


