package com.heena.user.models

import java.io.Serializable

data class Service(	val image: String? = null,
                       val address: String? = null,
                       val name: String? = null,
                       val username: String? = null,
                       val mangaer_id: Int? = null,
                       val comment_star: Int? = null,
                       val user_id: String? = null,
                       val price: Price? = null,
                       val gallery: List<String?>? = null,
                       val service_id: String? = null,
                       val description: String? = null,
                       val category: Category? = null,
                       val lat: String? = null,
                       val long: String? = null): Serializable
