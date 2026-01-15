package com.rentit.app.models.user

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val avatarUrl: String,
    val likedApartments: MutableList<String> = mutableListOf(),
    var lastUpdated: Long? = null
) {

    companion object {
        private const val NAME_KEY = "name"
        private const val PHONE_NUMBER_KEY = "phoneNumber"
        private const val EMAIL_KEY = "email"
        private const val AVATAR_URL_KEY = "avatarUrl"
        const val LIKED_APARTMENTS_KEY = "likedApartments"

        fun fromJson(json: Map<String, Any?>, id: String): User {
            val name = json[NAME_KEY] as? String ?: ""
            val phoneNumber = json[PHONE_NUMBER_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val avatarUrl = json[AVATAR_URL_KEY] as? String ?: ""
            val likedApartments = json[LIKED_APARTMENTS_KEY] as? MutableList<String> ?: mutableListOf()

            return User(id, name, phoneNumber, email, avatarUrl, likedApartments)
        }
    }

    val toJson: Map<String, Any>
        get() = hashMapOf(
            NAME_KEY to name,
            PHONE_NUMBER_KEY to phoneNumber,
            EMAIL_KEY to email,
            AVATAR_URL_KEY to avatarUrl,
            LIKED_APARTMENTS_KEY to likedApartments
        )
}