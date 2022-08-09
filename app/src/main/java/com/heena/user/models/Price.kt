package com.heena.user.models

import java.io.Serializable

data class Price(
    val main: String? = null,
    val total: String? = null,
    val child_price: String? = null,
) : Serializable
