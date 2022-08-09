package com.heena.user.models

import java.io.Serializable

data class ViewCardResponse(
    val message: String? = null,
    val status: Int? = null,
    val cards : List<Cards?>?=null
): Serializable
