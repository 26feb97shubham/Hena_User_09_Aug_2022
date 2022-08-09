package com.heena.user.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ServiceShowResponse(
	val bank: Bank? = null,
	val manager: Manager? = null,
	val service: Service? = null,
	val offer: OfferItemZ? = null,
	val commission: String? = null,
	val message: String? = null,
	val category: List<Any?>? = null,
	val gallery: List<GalleryItem?>? = null,
	val status: Int? = null
): Serializable

data class Dates (

	@SerializedName("id"               ) var id             : Int?                  = null,
	@SerializedName("service_id"       ) var serviceId      : Int?                  = null,
	@SerializedName("user_id"          ) var userId         : Int?                  = null,
	@SerializedName("manager_id"       ) var managerId      : Int?                  = null,
	@SerializedName("c_ladies"         ) var cLadies        : Int?                  = null,
	@SerializedName("c_children"       ) var cChildren      : Int?                  = null,
	@SerializedName("address_id"       ) var addressId      : Int?                  = null,
	@SerializedName("adr_location"     ) var adrLocation    : String?               = null,
	@SerializedName("adr_lat"          ) var adrLat         : String?               = null,
	@SerializedName("adr_long"         ) var adrLong        : String?               = null,
	@SerializedName("b_date"           ) var bDate          : String?               = null,
	@SerializedName("b_from"           ) var bFrom          : String?               = null,
	@SerializedName("b_to"             ) var bTo            : String?               = null,
	@SerializedName("message"          ) var message        : String?               = null,
	@SerializedName("cancel_msg"       ) var cancelMsg      : String?               = null,
	@SerializedName("cancel_by_user"   ) var cancelByUser   : Int?                  = null,
	@SerializedName("price"            ) var price          : Price?                  = null,
	@SerializedName("commission"       ) var commission     : Int?                  = null,
	@SerializedName("base_price_lady"  ) var basePriceLady  : Int?                  = null,
	@SerializedName("base_price_child" ) var basePriceChild : Int?                  = null,
	@SerializedName("off_price_lady"   ) var offPriceLady   : Int?                  = null,
	@SerializedName("off_price_child"  ) var offPriceChild  : Int?                  = null,
	@SerializedName("payment"          ) var payment        : Int?                  = null,
	@SerializedName("status"           ) var status         : Int?                  = null,
	@SerializedName("created_at"       ) var createdAt      : String?               = null,
	@SerializedName("updated_at"       ) var updatedAt      : String?               = null,
	@SerializedName("booked_date"      ) var bookedDate     : String?               = null,
	@SerializedName("booked_time"      ) var bookedTime     : ArrayList<BookedTime> = arrayListOf()

)

data class BookedTime (

	@SerializedName("b_from" ) var bFrom : String? = null,
	@SerializedName("b_to"   ) var bTo   : String? = null

)

