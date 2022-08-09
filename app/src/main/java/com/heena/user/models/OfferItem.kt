package com.heena.user.models

import java.io.Serializable

data class OfferItem(
    val offer: Offer? = null,
    var is_favorite: Int? = null,
    val price: Price? = null,
    val service_id: Int? = null,
    val name: String? = null,
    val category: Category? = null,
    val gallery: List<String>? = null
): Serializable