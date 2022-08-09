package com.heena.user.models

import java.io.Serializable

data class Offer(val price: Price? = null,
                 val started_at: String? = null,
                 val offer_id: Int? = null,
                 val ended_at: String? = null
) : Serializable
