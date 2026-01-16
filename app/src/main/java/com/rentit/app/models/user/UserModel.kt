package com.rentit.app.models.user

import android.util.Log
import com.rentit.app.models.FireStoreModel
import com.rentit.app.models.auth.AuthModel
import com.google.firebase.firestore.FieldValue
import com.rentit.app.base.Completion
import com.rentit.app.base.UsersCompletion
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.collections.remove
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserModel {
    private val firebaseDB = FireStoreModel.instance.db
    var currentUser: User? = null

    private companion object {
        const val TAG = "UserModel"
        const val USERS_COLLECTION_PATH = "users"
        val instance: UserModel = UserModel()
    }

    fun getAllUsers(completion: UsersCompletion) {
        firebaseDB.collection(USERS_COLLECTION_PATH)
            .get().addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> completion(result.result.map { User.fromJson(it.data) })
                    false -> completion(emptyList())
                }
            }
    }


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

    suspend fun addUser(user: User) {
        Log.d(TAG, "add user: $user")
        firebaseDB.collection(USERS_COLLECTION_PATH)
            .document(user.id)
            .set(user.toJson).await()
    }

    suspend fun updateMe(updateUserInput: UpdateUserInput) {
        val userId = AuthModel.instance.getUserId() ?: return
        Log.d(TAG, "update user with data: $updateUserInput")
        firebaseDB.collection(USERS_COLLECTION_PATH).document(userId).update(updateUserInput.toJson)
        getMe()
    }

    suspend fun getMe() {
        Log.d(TAG, "get me")
        val userId = AuthModel.instance.getUserId() ?: return
        currentUser = getUserById(userId)
    }

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

    suspend fun addLikedApartment(apartmentId: String) {
        val userId = AuthModel.instance.getUserId() ?: return
        suspendCoroutine { continuation ->
            firebaseDB.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .update(User.LIKED_APARTMENTS_KEY, FieldValue.arrayUnion(apartmentId))
                .addOnSuccessListener {
                    currentUser?.likedApartments?.add(apartmentId)
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    suspend fun removeLikedApartment(apartmentId: String) {
        val userId = AuthModel.instance.getUserId() ?: return
        suspendCoroutine { continuation ->
            firebaseDB.collection(USERS_COLLECTION_PATH)
                .document(userId)
                .update(User.LIKED_APARTMENTS_KEY, FieldValue.arrayRemove(apartmentId))
                .addOnSuccessListener {
                    currentUser?.likedApartments?.remove(apartmentId)
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}