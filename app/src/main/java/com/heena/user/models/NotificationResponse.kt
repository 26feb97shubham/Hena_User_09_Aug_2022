package com.heena.user.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationResponse  (
    @SerializedName("error"        ) var error        : Error?                  = Error(),
    @SerializedName("status"       ) var status       : Int?                    = null,
    @SerializedName("message"      ) var message      : String?                 = null,
    @SerializedName("notification" ) var notification : ArrayList<Notification> = arrayListOf()
): Serializable
data class Notification (

    @SerializedName("notification_id" ) var notificationId : Int?    = null,
    @SerializedName("title"           ) var title          : String? = null,
    @SerializedName("title_ar"           ) var titleAr          : String? = null,
    @SerializedName("message"         ) var message        : String? = null,
    @SerializedName("message_ar"         ) var messageAr        : String? = null,
    @SerializedName("is_new"          ) var isNew          : Int?    = null,
    @SerializedName("create_at"       ) var createAt       : String? = null,
    @SerializedName("comment_id"      ) var commentId      : Int?    = null,
    @SerializedName("user"            ) var user           : UserNotification?   = UserNotification()

): Serializable

data class UserNotification (

    @SerializedName("name"        ) var name       : String? = null,
    @SerializedName("receiver_id" ) var receiverId : Int?    = null

): Serializable