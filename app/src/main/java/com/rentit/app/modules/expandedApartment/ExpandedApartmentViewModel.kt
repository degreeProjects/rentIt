package com.rentit.app.modules.expandedApartment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.apartment.ApartmentModel

/**
 * ExpandedApartmentViewModel
 *
 * ViewModel that manages data for the expanded apartment detail view.
 * Fetches and holds a single apartment's data for display.
 */
class ExpandedApartmentViewModel: ViewModel() {
    // LiveData containing the apartment details to display
    var apartment: LiveData<Apartment>? = null

    // loads apartment data from the model by apartment ID
    fun setApartment(apartmentId: String) {
        apartment = ApartmentModel.instance.getApartment(apartmentId)
    }
}