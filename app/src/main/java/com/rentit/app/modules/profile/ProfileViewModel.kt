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

class ProfileViewModel: ViewModel() {
    var user: MutableLiveData<User> = MutableLiveData<User>()

    fun getCurrentUser() {
        // Use cached data - already fresh from app startup
        user.value = UserModel.instance.currentUser
    }

    fun updateCurrentUser(updateUserInput: UpdateUserInput, avatarUri: Uri?) {
        viewModelScope.launch {

            if (avatarUri != null) {
                updateUserInput.avatarUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(avatarUri, FirebaseStorageModel.USERS_PATH)
            }

            UserModel.instance.updateMe(updateUserInput)
            user.postValue(UserModel.instance.currentUser)

        }
    }
}