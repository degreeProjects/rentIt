package com.rentit.app.modules.apartments.myApartments

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import com.rentit.app.modules.apartments.base.BaseApartmentsFragment

/**
 * Fragment responsible for displaying a list of apartments owned or managed by the current user.
 * Inherits common logic from [BaseApartmentsFragment].
 */
class MyApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "MyApartmentsFragment"

    /**
     * Callback function triggered when the "Edit" action is performed on an apartment item.
     * Navigates to the EditApartmentFragment and passes the specific apartmentId via a Bundle.
     */
    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(
            R.id.action_myApartmentsFragment_to_editApartmentFragment,
            bundleOf("apartmentId" to it)
        )
    }

    /**
     * Initializes the [ApartmentsRecyclerAdapter] specific to this fragment.
     * Uses the user's specific apartments from the ViewModel and attaches the [onEditClick] listener.
     */
    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        return ApartmentsRecyclerAdapter(viewModel.getMyApartments(), viewModel, onEditClick)
    }

    /**
     * Sets up the LiveData observer for the apartments list.
     * When data changes:
     * 1. Refreshes the adapter's data set.
     * 2. Notifies the UI of the change.
     */
    override fun observeApartments() {
        viewModel.apartments?.observe(viewLifecycleOwner) {
            adapter.apartments = viewModel.getMyApartments()
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Defines the behavior for clicking on an entire apartment item in the list.
     * Maps the clicked position to an apartment object and navigates to the detailed view
     * (ExpandedApartmentFragment) using the apartment's unique ID.
     */
    override fun setupApartmentsAdapterListener(): OnItemClickListener {
        return object: OnItemClickListener {
            override fun onItemClick(apartmentId: Int) {
                Log.d(TAG, "ApartmentsRecyclerAdapter: apartment id is $apartmentId")
                val apartment = viewModel.apartments?.value?.get(apartmentId)
                apartment?.let {
                    findNavController().navigate(
                        R.id.action_MyApartmentsFragment_to_expandedApartmentFragment,
                        bundleOf("apartmentId" to apartment.id)
                    )
                }
            }
        }
    }
}