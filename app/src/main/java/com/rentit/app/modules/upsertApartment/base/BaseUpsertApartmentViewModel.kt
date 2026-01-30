package com.rentit.app.modules.upsertApartment.base

import androidx.lifecycle.LiveData // LiveData type for observing apartment changes
import androidx.lifecycle.ViewModel // Base ViewModel class
import com.rentit.app.models.apartment.Apartment // Apartment model
import com.rentit.app.models.apartment.ApartmentModel // Model/repository for apartment operations

// ViewModel that loads a single apartment for the edit flow
class BaseUpsertApartmentViewModel: ViewModel() { // Simple ViewModel (no SavedStateHandle here)
    var apartment: LiveData<Apartment>? = null // Holds the observable apartment (null until set)

    fun setApartment(apartmentId: String) { // Set the current apartment by id
        apartment = ApartmentModel.instance.getApartment(apartmentId) // Fetch apartment LiveData from the model/repository
    }
}