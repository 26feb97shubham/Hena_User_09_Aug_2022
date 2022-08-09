package com.heena.user.models

import java.io.Serializable

data class Manager(
    val image: String? = null,
    val name: String? = null,
    val avg_star: String? = null,
    val manager_id: Int? = null,
    val star: Any? = null,
    val username: String? = null
):Serializable
