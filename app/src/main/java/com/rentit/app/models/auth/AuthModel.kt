package com.rentit.app.models.auth

import android.util.Log
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class AuthModel {
    private val auth: FirebaseAuth = Firebase.auth
    companion object {
        val instance: AuthModel = AuthModel()
        const val TAG = "AuthModel"
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        Log.d(TAG, "user sign up with email: $email")
        return auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        Log.d(TAG, "user sign in with email: $email")
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        Log.d(TAG, "logout user")
        auth.signOut()
    }

    fun getUser() = auth.currentUser

    fun getUserId() = auth.currentUser?.uid
}