package com.rentit.app.modules.apartments.myApartments

import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import com.rentit.app.modules.apartments.base.BaseApartmentsFragment

/**
 * MyApartmentsFragment
 * 
 * Displays apartments that belong to the current user.
 * Extends BaseApartmentsFragment to inherit common apartment list functionality.
 */
class MyApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "MyApartmentsFragment"

     // navigates to the edit apartment fragment
    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(
            R.id.action_myApartmentsFragment_to_editApartmentFragment,
            bundleOf("apartmentId" to it)
        )
    }

    // Sets up the RecyclerView adapter with only apartments owned by the current user
    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        return ApartmentsRecyclerAdapter(viewModel.getMyApartments(), viewModel, onEditClick)
    }

    // observes changes to apartments and updates the list to show only user's apartments
    override fun observeApartments() {
        viewModel.apartments?.observe(viewLifecycleOwner) {
            adapter.apartments = viewModel.getMyApartments()
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Sets up click listener for user's apartment items.
     * Navigates to expanded apartment view when clicked.
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