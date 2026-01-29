package com.rentit.app.modules.apartments.likedApartments

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import com.rentit.app.modules.apartments.base.BaseApartmentsFragment

/**
 * Fragment responsible for displaying the list of apartments a user has marked as Liked.
 * Inherits shared UI logic and setup from [BaseApartmentsFragment].
 */
class LikedApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "LikedApartmentsFragment"

    /**
     * Lambda function passed to the adapter to handle edit actions.
     * When triggered, it navigates the user to the EditApartmentFragment,
     * passing the [apartmentId] through a Navigation Bundle.
     */
    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(
            R.id.action_likedApartmentsFragment_to_editApartmentFragment,
            bundleOf("apartmentId" to it)
        )
    }

    /**
     * Configures the [ApartmentsRecyclerAdapter] specifically for this fragment.
     * It fetches the liked apartments list from the ViewModel and attaches the [onEditClick] listener.
     */
    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        return ApartmentsRecyclerAdapter(viewModel.getLikedApartments(), viewModel, onEditClick)
    }

    /**
     * Sets up a LiveData observer on the apartments list.
     * When the data changes:
     * 1. The progress bar is shown.
     * 2. The adapter's data is updated with the current "Liked" list.
     * 3. The UI is refreshed via [notifyDataSetChanged].
     * 4. The progress bar is hidden.
     */
    override fun observeApartments() {
        viewModel.apartments?.observe(viewLifecycleOwner) {
            progressBar.visibility = View.VISIBLE
            adapter.apartments = viewModel.getLikedApartments()
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }
    }

    /**
     * Sets up the click listener for individual items in the RecyclerView.
     * When an apartment is clicked:
     * 1. It identifies the specific apartment object via its position (apartmentId).
     * 2. It navigates to the ExpandedApartmentFragment to show full details.
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