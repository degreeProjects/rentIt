package com.rentit.app.modules.upsertApartment.editApartment

import androidx.navigation.fragment.navArgs
import com.rentit.app.modules.upsertApartment.base.BaseUpsertApartmentFragment

/**
 * EditApartmentFragment
 *
 * Fragment for editing an existing apartment.
 * Receives apartment ID from navigation arguments and loads existing data.
 */
class EditApartmentFragment : BaseUpsertApartmentFragment("EditApartmentFragment") {
    private val args: EditApartmentFragmentArgs by navArgs()

    // provides apartment ID to base fragment for loading existing apartment data
    override fun getApartmentId(): String {
        return args.apartmentId
    }
}
