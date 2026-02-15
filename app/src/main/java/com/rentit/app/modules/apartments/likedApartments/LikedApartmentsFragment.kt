package com.rentit.app.modules.apartments.likedApartments

import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import com.rentit.app.modules.apartments.base.BaseApartmentsFragment

/**
 * LikedApartmentsFragment
 * 
 * Displays apartments that the current user has marked as liked.
 * Extends BaseApartmentsFragment to inherit common apartment list functionality.
 */
class LikedApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "LikedApartmentsFragment"

     // navigates to the edit apartment fragment.
    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(
            R.id.action_likedApartmentsFragment_to_editApartmentFragment,
            bundleOf("apartmentId" to it)
        )
    }

    // Sets up the RecyclerView adapter with only liked apartments
    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        return ApartmentsRecyclerAdapter(viewModel.getLikedApartments(), viewModel, onEditClick)
    }

    // observes changes to apartments and updates the list to show only liked ones
    override fun observeApartments() {
        viewModel.apartments?.observe(viewLifecycleOwner) {
            adapter.apartments = viewModel.getLikedApartments()
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Sets up click listener for liked apartment items.
     * Navigates to expanded apartment view when clicked.
     */
    override fun setupApartmentsAdapterListener(): OnItemClickListener {
        return object: OnItemClickListener {
            override fun onItemClick(apartmentId: Int) {
                Log.d(TAG, "ApartmentsRecyclerAdapter: apartment id is $apartmentId")
                val apartment = viewModel.apartments?.value?.get(apartmentId)
                apartment?.let {
                    findNavController().navigate(
                        R.id.action_likedApartmentsFragment_to_expandedApartmentFragment,
                        bundleOf("apartmentId" to apartment.id)
                    )
                }
            }
        }
    }
}