package com.rentit.app.modules.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentit.app.models.FirebaseStorageModel
import com.rentit.app.models.user.UpdateUserInput
import com.rentit.app.models.user.User
import com.rentit.app.models.user.UserModel
import kotlinx.coroutines.launch

/**
 * ProfileViewModel
 *
 * ViewModel that manages user profile data and update operations.
 * Handles avatar upload to Firebase Storage and user data updates.
 */
class ProfileViewModel: ViewModel() {
    // LiveData containing the current user's profile data
    var user: MutableLiveData<User> = MutableLiveData<User>()

    // loads current user data from cache
    fun getCurrentUser() {
        // Use cached data - already fresh from app startup
        user.value = UserModel.instance.currentUser
    }

    // updates user profile data and uploads new avatar to Firebase Storage if provided
    fun updateCurrentUser(updateUserInput: UpdateUserInput, avatarUri: Uri?) {
        viewModelScope.launch {
            // upload new avatar to firebase storage if selected
            if (avatarUri != null) {
                updateUserInput.avatarUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(avatarUri, FirebaseStorageModel.USERS_PATH)
            }

            UserModel.instance.updateMe(updateUserInput)
            user.postValue(UserModel.instance.currentUser)

        }
    }
}