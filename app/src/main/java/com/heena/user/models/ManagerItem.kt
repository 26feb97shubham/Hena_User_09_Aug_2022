package com.heena.user.models

import java.io.Serializable

data class ManagerItem(
        val image: String? = null,
        val address: String? = null,
        val name: String? = null,
        val username: String? = null,
        val email: String? = null,
        val mangaer_id: Int? = null,
        val comment_star: Int? = null
): Serializable