package com.heena.user.models

import java.io.Serializable

data class Booking(
    val address: AddressItemX? = null,
    val cancelled_by: Int? = null,
    val manager: Manager? = null,
    val address_id: Int? = null,
    val c_children: Int? = null,
    val booking_id: Int? = null,
    val c_ladies: Int? = null,
    val user_id: Int? = null,
    val manager_id: Int? = null,
    val service: Service? = null,
    val service_id: Int? = null,
    val payment: Int? = null,
    val location: Location? = null,
    val gallery: List<String?>? = null,
    val dates: List<String?>? = null,
    var status: Int? = null,
    val price : PriceX? = null,
    val quantity : Quantity? = null,
    val booking_to : String? = null,
    val cancel_msg : String? = null,
    val booking_date : String? = null,
    val booking_from : String? = null,
    val booking_created_at : String? = null,
    val message : String? = null,
    val card: CardsX
) : Serializable
