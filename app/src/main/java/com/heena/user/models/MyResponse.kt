package com.heena.user.models

import java.io.Serializable

data class MyResponse(
    val message: String? = null,
    val status: Int? = null
):Serializable
