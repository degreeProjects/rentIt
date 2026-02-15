package com.rentit.app.modules.apartments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.apartment.ApartmentModel
import com.rentit.app.models.user.UserModel
import kotlinx.coroutines.launch

/**
 * ApartmentsViewModel
 * 
 * ViewModel that manages apartment data and business logic for apartment-related fragments.
 * 
 * This ViewModel is shared across all apartment fragments (ApartmentsFragment,
 * LikedApartmentsFragment, MyApartmentsFragment) to maintain consistent state.
 */
class ApartmentsViewModel: ViewModel() {
    // LiveData containing all apartments, observed by fragments for real-time updates
    var apartments: LiveData<MutableList<Apartment>>? = null

    /**
     * Handles like/unlike action on an apartment.
     * Updates both user's liked apartments list and apartment's liked status.
     */
    fun onLikeClick(apartmentId: String, liked: Boolean) {
        viewModelScope.launch {
            if (liked) {
                UserModel.instance.addLikedApartment(apartmentId)
                ApartmentModel.instance.setApartmentLiked(apartmentId, true)
            } else {
                UserModel.instance.removeLikedApartment(apartmentId)
                ApartmentModel.instance.setApartmentLiked(apartmentId, false)
            }
        }
    }

    // handles deletion of an apartment
    fun onDeleteClick(apartmentId: String) {
        viewModelScope.launch {
            // First remove the apartment from all users who have liked it
            UserModel.instance.removeApartmentFromAllUsers(apartmentId)
            // Then delete the apartment
            ApartmentModel.instance.deleteApartment(apartmentId)
        }
    }

    //Initializes the apartments LiveData by fetching all apartments from the model.
    suspend fun setAllApartments() {
        apartments = ApartmentModel.instance.getAllApartments()
    }

    /**
     * Returns all apartments with user-specific flags (liked, isMine) set.
     * Iterates through all apartments and marks them based on current user's data.
     */
    fun getAllApartments(): MutableList<Apartment> {
        val allApartments = mutableListOf<Apartment>()
        val currentUser = UserModel.instance.currentUser
        
        Log.d("ApartmentsViewModel", "getAllApartments called, currentUser: ${currentUser?.id ?: "null"}")
        Log.d("ApartmentsViewModel", "apartments LiveData value size: ${apartments?.value?.size ?: 0}")

        for (apartment in apartments?.value ?: mutableListOf()) {
            // Only set liked/isMine flags if user is logged in
            if (currentUser != null) {
                if (currentUser.likedApartments.contains(apartment.id)) {
                    apartment.liked = true
                }

                if (currentUser.id == apartment.userId) {
                    apartment.isMine = true
                }
            }

            allApartments.add(apartment)
        }
        
        Log.d("ApartmentsViewModel", "Returning ${allApartments.size} apartments")
        return allApartments
    }

    // Filters and returns only apartments that the current user has liked
    fun getLikedApartments(): MutableList<Apartment> {
        val likedApartmentsList = mutableListOf<Apartment>()
        val currentUser = UserModel.instance.currentUser ?: return likedApartmentsList


        for (apartment in apartments?.value ?: mutableListOf()) {
            if (currentUser.likedApartments.contains(apartment.id)) {
                apartment.liked = true
                likedApartmentsList.add(apartment)
            }
        }

        return likedApartmentsList
    }

    // Filters and returns only apartments owned by the current user.
    fun getMyApartments(): MutableList<Apartment> {
        val myApartmentsList = mutableListOf<Apartment>()
        val currentUser = UserModel.instance.currentUser ?: return myApartmentsList

        for (apartment in apartments?.value ?: mutableListOf()) {
            if (currentUser.id == apartment.userId) {
                apartment.isMine = true
                myApartmentsList.add(apartment)
            }
        }

        return myApartmentsList
    }

    // Refreshes the apartments list by fetching latest data from firebase
    suspend fun refreshAllApartments() {
        ApartmentModel.instance.refreshAllApartments()
    }
}