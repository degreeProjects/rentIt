package com.rentit.app.modules.expandedApartment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.apartment.ApartmentModel

class ExpandedApartmentViewModel: ViewModel() {
    var apartment: LiveData<Apartment>? = null

    fun setApartment(apartmentId: String) {
        apartment = ApartmentModel.instance.getApartment(apartmentId)
    }
}