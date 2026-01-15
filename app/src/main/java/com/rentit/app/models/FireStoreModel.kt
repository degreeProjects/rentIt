package com.rentit.app.models

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FireStoreModel {
    val db = Firebase.firestore
    companion object {
        val instance: FireStoreModel = FireStoreModel()
    }

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }

        db.firestoreSettings = settings
    }
}