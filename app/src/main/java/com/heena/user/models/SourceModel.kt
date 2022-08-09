package com.heena.user.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SourceModel(
    @SerializedName("object" ) var myobject : String? = null,
    @SerializedName("id"     ) var id     : String? = null,
    @SerializedName("type"     ) var type     : String? = null
): Serializable
