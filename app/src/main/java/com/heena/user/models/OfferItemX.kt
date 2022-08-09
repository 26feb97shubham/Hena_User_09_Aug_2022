package com.heena.user.models

import java.io.Serializable

data class OfferItemZ(
    val started_at: String? = null,
    val id: Int? = null,
    val user_id: Int? = null,
    val service_id: Int? = null,
    val price: Double? = null,
    val offer_price: Double? = null,
    val child_price : Double? = null,
    val percentage : Int? = null,
    val ended_at: String? = null
):Serializable
