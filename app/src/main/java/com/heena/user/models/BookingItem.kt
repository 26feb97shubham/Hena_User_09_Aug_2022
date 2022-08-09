package com.heena.user.models

import java.io.Serializable

data class BookingItem(
    val booking_id: Int? = null,
    val price: Double? = null,
    val booking_to: String? = null,
    val booking_date: String? = null,
    val booking_from: String? = null,
    val booking_created_at: String? = null,
    val manager: Manager? = null,
    val service: Service? = null,
    val payment: Int? = null,
    val cancel_msg: String? = null,
    val cancelled_by: Int? = null,
    val status: Int? = null
): Serializable
