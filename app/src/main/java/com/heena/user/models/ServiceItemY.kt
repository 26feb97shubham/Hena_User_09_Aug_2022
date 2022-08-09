package com.heena.user.models

import java.io.Serializable

data class ServiceItemY(
    var is_favorite: Int? = null,
    val distance: String? = null,
    val price: Price? = null,
    val service_id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val category: Category? = null,
    val user: User? = null,
    val gallery: List<String?>? = null,
    val offer: Offer? = null
):Serializable
