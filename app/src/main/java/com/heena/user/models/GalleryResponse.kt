package com.heena.user.models

import java.io.Serializable

data class GalleryResponse(
	val message: String? = null,
	val gallery: List<GalleryItem?>? = null,
	val status: Int? = null
): Serializable



