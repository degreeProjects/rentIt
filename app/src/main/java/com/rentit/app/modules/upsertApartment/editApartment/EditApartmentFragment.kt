package com.rentit.app.modules.upsertApartment.editApartment // Package for "edit apartment" feature module

import androidx.navigation.fragment.navArgs // SafeArgs delegate for reading navigation arguments
import com.rentit.app.modules.upsertApartment.base.BaseUpsertApartmentFragment // Shared upsert fragment base

// Fragment for editing an existing apartment
class EditApartmentFragment : BaseUpsertApartmentFragment("EditApartmentFragment") { // Pass a tag for logging/debugging
    private val args: EditApartmentFragmentArgs by navArgs() // Read SafeArgs (contains apartmentId)

    override fun getApartmentId(): String { // Provide apartment id to base so it loads and edits existing item
        return args.apartmentId // Return id passed via navigation arguments
    }
}