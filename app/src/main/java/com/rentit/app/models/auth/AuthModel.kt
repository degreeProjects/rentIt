package com.rentit.app.models.auth

import android.util.Log
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

// Singleton class managing user authentication with Firebase Auth.
class AuthModel {
    private val auth: FirebaseAuth = Firebase.auth
    companion object {
        val instance: AuthModel = AuthModel()
        const val TAG = "AuthModel"
    }

    // Creates a new user account with email and password.
    // Returns AuthResult containing user data on success.
    suspend fun signUp(email: String, password: String): AuthResult {
        Log.d(TAG, "user sign up with email: $email")
        return auth.createUserWithEmailAndPassword(email, password).await()
    }

    // Signs in an existing user with email and password.
    // Returns AuthResult containing user data on success.
    suspend fun signIn(email: String, password: String): AuthResult {
        Log.d(TAG, "user sign in with email: $email")
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    // Signs out the current user from Firebase Auth.
    fun signOut() {
        Log.d(TAG, "logout user")
        auth.signOut()
    }

    // Returns the currently authenticated Firebase user or null if not signed in.
    fun getUser() = auth.currentUser

    // Returns the unique ID of the currently authenticated user or null if not signed in.
    fun getUserId() = auth.currentUser?.uid
}