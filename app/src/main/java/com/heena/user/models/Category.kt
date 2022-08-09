package com.heena.user.models

import java.io.Serializable

data class Category(
        val category_id: Int? = null,
        val name: String? = null,
        val name_ar: String? = null
) : Serializable


