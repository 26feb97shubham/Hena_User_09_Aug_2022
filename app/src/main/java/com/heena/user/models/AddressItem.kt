package com.heena.user.models

import java.io.Serializable

data class AddressItem(
    val flat: String? = null,
    val street: String? = null,
    val address_id: Int? = null,
    val created_at: String? = null,
    val title: String? = null,
    val is_default: Int? = null
): Serializable

