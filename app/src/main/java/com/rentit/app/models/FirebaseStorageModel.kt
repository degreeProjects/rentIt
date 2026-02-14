package com.rentit.app.models

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.UUID

// Singleton class managing image uploads to Firebase Storage.
class FirebaseStorageModel {
    private val storage = Firebase.storage
    companion object {
        val instance: FirebaseStorageModel = FirebaseStorageModel()
        const val USERS_PATH = "users"
        const val APARTMENTS_PATH = "apartments"
    }

    // Uploads an image to Firebase Storage and returns its download URL.
    // Generates a unique filename using UUID to avoid conflicts.
    // Returns empty string if upload fails.
    suspend fun addImageToFirebaseStorage(uri: Uri, path: String): String {
        return try {
            val filename = UUID.randomUUID().toString()
            Log.d("FirebaseStorage", "Uploading image: $filename from URI: $uri")

            val imagesRef = storage.reference.child("$path/$filename") // Create a reference to the storage location where the image will be saved
            val uploadTask = imagesRef.putFile(uri).await() // Upload the file from the URI and wait for completion
            val downloadUrl = uploadTask.storage.downloadUrl.await() // Get the public download URL after upload completes
            downloadUrl.toString() // Convert the URL to a string and return it
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error uploading image: ${e.message}", e)
            ""
        }
    }
}