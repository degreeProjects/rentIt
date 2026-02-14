package com.rentit.app.models

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

// Singleton class providing configured Firestore database instance.
// Initializes Firestore with memory caching for improved performance.
class FireStoreModel {
    val db = Firebase.firestore
    companion object {
        val instance: FireStoreModel = FireStoreModel()
    }

    init {
        // Configure Firestore with memory cache for offline support and faster data access
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        db.firestoreSettings = settings
    }
}