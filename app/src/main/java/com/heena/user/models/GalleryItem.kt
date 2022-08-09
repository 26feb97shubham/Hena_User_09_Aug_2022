package com.heena.user.models

import java.io.Serializable

data class GalleryItem(
    val image: String? = null,
    val name: String? = null,
    val gallery_id: Int? = null
): Serializable