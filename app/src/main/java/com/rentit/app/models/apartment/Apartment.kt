package com.rentit.app.models.apartment

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Type(type: String) {
    Apartment("Apartment"),
    House("House"),
    Villa("Villa"),
    Penthouse("Penthouse"),
}

// Room entity representing an apartment listing.
// Stored locally and synced with Firestore.
@Entity
data class Apartment(
    @PrimaryKey val id: String,
    val userId: String,
    var title: String,
    var pricePerNight: Int,
    var description: String,
    var city: String,
    var type: Type,
    var numOfRooms: Int,
    var startDate: Long,
    var endDate: Long,
    var imageUrl: String,
    var liked: Boolean = false,
    var isMine: Boolean = false
) {

    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val USER_ID_KEY = "userId"
        private const val PRICE_PER_NIGHT_KEY = "pricePerNight"
        private const val DESCRIPTION_KEY = "description"
        private const val CITY_KEY = "city"
        private const val TYPE_KEY = "type"
        private const val NUM_OF_ROOMS_KEY = "numOfRooms"
        private const val START_DATE_KEY = "startDate"
        private const val END_DATE_KEY = "endDate"
        private const val IMAGE_URL_KEY = "imageUrl"

        // Converts JSON map from Firestore into Apartment object.
        // Handles missing or invalid values gracefully with defaults.
        fun fromJson(json: Map<String, Any>): Apartment {
            Log.d("Apartment", "Parsing apartment from JSON: $json")
            
            val id = json[ID_KEY] as? String ?: ""
            val title = json[TITLE_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val pricePerNight = (json[PRICE_PER_NIGHT_KEY] as? Long)?.toInt() ?: 0
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val city = json[CITY_KEY] as? String ?: ""
            
            // Safe type parsing - handles null, missing, or invalid type values
            val typeString = json[TYPE_KEY] as? String
            val type = if (typeString != null) {
                try {
                    Type.valueOf(typeString)
                } catch (e: IllegalArgumentException) {
                    Log.w("Apartment", "Unknown type value: $typeString, defaulting to House")
                    Type.House
                }
            } else {
                Log.w("Apartment", "Type is null, defaulting to House")
                Type.House
            }
            
            val numOfRooms = (json[NUM_OF_ROOMS_KEY] as? Long)?.toInt() ?: 0
            val startDate = json[START_DATE_KEY] as? Long ?: 0
            val endDate = json[END_DATE_KEY] as? Long ?: 0
            val imageUrl = json[IMAGE_URL_KEY] as? String ?: ""

            Log.d("Apartment", "Successfully parsed apartment: id=$id, title=$title")
            return Apartment(id, userId, title, pricePerNight, description, city, type, numOfRooms, startDate, endDate, imageUrl)
        }
    }
    
    // Converts Apartment object to JSON map for Firestore storage.
    val toJson: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            TITLE_KEY to title,
            USER_ID_KEY to userId,
            PRICE_PER_NIGHT_KEY to pricePerNight,
            DESCRIPTION_KEY to description,
            CITY_KEY to city,
            TYPE_KEY to type.name,
            NUM_OF_ROOMS_KEY to numOfRooms,
            START_DATE_KEY to startDate,
            END_DATE_KEY to endDate,
            IMAGE_URL_KEY to imageUrl,
        )
}