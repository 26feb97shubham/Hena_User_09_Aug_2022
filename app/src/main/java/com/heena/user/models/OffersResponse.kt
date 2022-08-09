package com.heena.user.models

import java.io.Serializable

data class OffersResponse(
	val offer: List<OfferItemX?>? = null,
	val error: Error? = null,
	val message: String? = null,
	val status: Int? = null
): Serializable

data class OfferItemX(
	val offer: OfferY? = null,
	val is_favorite: Int? = null,
	val price: PriceY? = null,
	val service_id: Int? = null,
	val name: String? = null,
	val category: CategoryX? = null,
	val user: UserY? = null,
	val gallery: List<String?>? = null
): Serializable

data class CategoryX(
	val category_id: Int? = null,
	val name: String? = null
): Serializable

data class PriceY(
	val total: String? = null,
	val child_price: String? = null,
	val main: String? = null
): Serializable

data class OfferY(
	val price: PriceY? = null,
	val percentage: Int? = null,
	val started_at: String? = null,
	val offer_id: Int? = null,
	val ended_at: String? = null
): Serializable

data class UserY(
	val image: String? = null,
	val name: String? = null,
	val avg_star: String? = null
): Serializable

