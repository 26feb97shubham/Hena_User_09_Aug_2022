package com.heena.user.models

import java.io.Serializable

data class Profile(
        val image: String? = null,
        val phone: String? = null,
        val user_id: Int? = null,
        val join_date: String? = null,
        val country_code: String? = null,
        val name: String? = null,
        val email: String? = null,
        val username: String? = null,
        val is_favorite: String? = null,
        val comment_avg: String? = null
) : Serializable