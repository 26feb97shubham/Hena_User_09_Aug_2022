package com.heena.user.models

import java.io.Serializable

data class User(
        val user_id: Int? = null,
        val image: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val category_id: String? = null,
        val avg_star: String? = null,
        val name: String,
) : Serializable
