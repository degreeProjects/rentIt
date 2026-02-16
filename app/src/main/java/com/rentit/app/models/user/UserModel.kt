package com.rentit.app.models.user

import android.util.Log
import com.rentit.app.models.FireStoreModel
import com.rentit.app.models.auth.AuthModel
import com.google.firebase.firestore.FieldValue
import com.rentit.app.base.Completion
import com.rentit.app.base.UsersCompletion
import com.squareup.picasso.Picasso
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// Singleton class managing user data and operations with Firestore.
class UserModel private constructor() {
    private val firebaseDB = FireStoreModel.instance.db
    var currentUser: User? = null

    companion object {
        const val TAG = "UserModel"
        const val USERS_COLLECTION_PATH = "users"
        val instance: UserModel = UserModel()
    }

    // Adds a new user to Firestore database.
    suspend fun addUser(user: User) {
        Log.d(TAG, "add user: $user")
        firebaseDB.collection(USERS_COLLECTION_PATH)
            .document(user.id)
            .set(user.toJson).await()
    }

    // Updates the current user's profile data in Firestore and refreshes local cache.
    suspend fun updateMe(updateUserInput: UpdateUserInput) {
        val userId = AuthModel.instance.getUserId() ?: return
        Log.d(TAG, "update user with data: $updateUserInput")
        firebaseDB.collection(USERS_COLLECTION_PATH).document(userId).update(updateUserInput.toJson).await()
        getMe()
    }

    // Fetches the current authenticated user's data and updates local cache.
    suspend fun getMe() {
        Log.d(TAG, "get me")
        val userId = AuthModel.instance.getUserId() ?: return
        currentUser = getUserById(userId)
        
        // Preload avatar image for better profile page performance
        preloadUserAvatar()
    }
    
    // Prefetches the user's avatar image for improved profile page loading.
    private fun preloadUserAvatar() {
        val avatarUrl = currentUser?.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            try {
                // Prefetch the avatar image so it's cached when user visits profile
                Picasso.get()
                    .load(avatarUrl)
                    .fetch()
            } catch (e: Exception) {
                // Silently fail - not critical if preload doesn't work
                Log.d(TAG, "Avatar preload skipped: ${e.message}")
            }
        }
    }

    // Retrieves a user by their ID from Firestore.
    // Returns null if user not found or error occurs.
    suspend fun getUserById(userId: String): User? {
        Log.d(TAG, "getUserById with id $userId")
        return try {
            val documentSnapshot = firebaseDB.collection(USERS_COLLECTION_PATH).document(userId).get().await()
            documentSnapshot.data?.let { User.fromJson(it) }
        } catch (e: Exception) {
            Log.e("TAG", "An unexpected error occurred: ${e.message}")
            null
        }
    }

    // Adds an apartment to the current user's liked apartments list.
    suspend fun addLikedApartment(apartmentId: String) {
        val userId = AuthModel.instance.getUserId() ?: return
        suspendCoroutine { continuation ->
            // Update Firestore using arrayUnion to prevent duplicates
            firebaseDB.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .update(User.LIKED_APARTMENTS_KEY, FieldValue.arrayUnion(apartmentId))
                .addOnSuccessListener {
                    currentUser?.likedApartments?.add(apartmentId) // Update local cache for immediate UI reflection
                    continuation.resume(Unit) // Resume coroutine with success doesnt return anything
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception) // Resume coroutine with error
                }
        }
    }

    // Removes an apartment from the current user's liked apartments list.
    suspend fun removeLikedApartment(apartmentId: String) {
        val userId = AuthModel.instance.getUserId() ?: return
        suspendCoroutine { continuation ->
            // Update Firestore using arrayRemove to safely remove the apartment
            firebaseDB.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .update(User.LIKED_APARTMENTS_KEY, FieldValue.arrayRemove(apartmentId))
                .addOnSuccessListener {
                    currentUser?.likedApartments?.remove(apartmentId) // Update local cache for immediate UI reflection
                    continuation.resume(Unit) // Resume coroutine with success doesnt return anything
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception) // Resume coroutine with error
                }
        }
    }

    // Removes an apartment from all users' liked apartments lists.
    suspend fun removeApartmentFromAllUsers(apartmentId: String) {
        try {
            Log.d(TAG, "Removing apartment $apartmentId from all users' liked apartments")
            // Get all users who have this apartment in their liked apartments
            val usersSnapshot = firebaseDB.collection(USERS_COLLECTION_PATH)
                .whereArrayContains(User.LIKED_APARTMENTS_KEY, apartmentId)
                .get()
                .await()

            // Remove the apartment from each user's liked apartments array
            for (document in usersSnapshot.documents) {
                firebaseDB.collection(USERS_COLLECTION_PATH)
                    .document(document.id)
                    .update(User.LIKED_APARTMENTS_KEY, FieldValue.arrayRemove(apartmentId))
                    .await()
            }
            Log.d(TAG, "Successfully removed apartment from ${usersSnapshot.size()} users")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing apartment from users: ${e.message}")
            throw e
        }
    }

//    fun getAllUsers(completion: UsersCompletion) {
//        firebaseDB.collection(USERS_COLLECTION_PATH)
//            .get().addOnCompleteListener { result ->
//                when (result.isSuccessful) {
//                    true -> completion(result.result.map { User.fromJson(it.data) })
//                    false -> completion(emptyList())
//                }
//            }
//    }
//
//
//    fun addUser(user: User, completion: Completion) {
//        Log.d(TAG, "add user: $user")
//        firebaseDB.collection(USERS_COLLECTION_PATH)
//            .document(user.id)
//            .set(user.toJson)
//            .addOnSuccessListener { documentReference ->
//            completion()
//        }
//            .addOnFailureListener { e ->
//                completion()
//            }
//    }

}