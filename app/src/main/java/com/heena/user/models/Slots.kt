package com.heena.user.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Slots(
    @SerializedName("fromTime" ) var fromTime : String?=null,
    @SerializedName("ToTime" )  var ToTime : String?=null,
    @SerializedName("fromTimeToTime" ) var fromTimeToTime : String?=null,
    @SerializedName("isChecked") var isChecked: Boolean = false
): Serializable
