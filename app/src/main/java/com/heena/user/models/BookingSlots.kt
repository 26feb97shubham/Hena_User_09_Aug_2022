package com.heena.user.models

import com.google.gson.annotations.SerializedName

data class BookingSlots(
    @SerializedName("fromTime" ) var fromTime : String?=null,
    @SerializedName("ToTime" )  var ToTime : String?=null
)
