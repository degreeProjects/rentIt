package com.rentit.app.modules.upsertApartment.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.apartment.ApartmentModel

/**
 * BaseUpsertApartmentViewModel
 *
 * ViewModel that loads a single apartment for the edit flow.
 * Provides LiveData for observing apartment data changes.
 */
class BaseUpsertApartmentViewModel: ViewModel() {
    // LiveData containing the apartment being edited
    var apartment: LiveData<Apartment>? = null

    // loads apartment data from the model by apartment ID
    fun setApartment(apartmentId: String) {
        apartment = ApartmentModel.instance.getApartment(apartmentId)
    }
}
