package com.rentit.app.modules.apartments
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.models.user.UserModel
import com.rentit.app.modules.apartments.adapter.ApartmentsRecyclerAdapter
import com.rentit.app.modules.apartments.adapter.OnItemClickListener
import com.rentit.app.modules.apartments.base.BaseApartmentsFragment

/**
 * ApartmentsFragment
 * 
 * Main fragment that displays all available apartments in the app.
 * Extends BaseApartmentsFragment to inherit common apartment list functionality.
 */
class ApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "ApartmentsFragment"

    // Navigates to the edit apartment fragment with the apartment ID
    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(R.id.action_apartmentsFragment_to_editApartmentFragment, bundleOf("apartmentId" to it))
    }

    /**
     * Performs initial preparations before displaying apartments.
     * Fetches current user data to determine ownership and liked status.
     */
    override suspend fun preparations() {
        return UserModel.instance.getMe()
    }

    //Creates adapter instance with edit functionality for owned apartments.
    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        val apartments = viewModel.getAllApartments()
        Log.d(TAG, "setupApartmentsAdapter: Creating adapter with ${apartments.size} apartments")
        return ApartmentsRecyclerAdapter(apartments, viewModel, onEditClick)
    }

    // listens to LiveData changes and refreshes the adapter when data changes.
    override fun observeApartments() {
        Log.d(TAG, "observeApartments: Setting up observer, apartments LiveData is ${if (viewModel.apartments != null) "not null" else "null"}")
        viewModel.apartments?.observe(viewLifecycleOwner) { apartmentList ->
            Log.d(TAG, "observeApartments: LiveData triggered with ${apartmentList?.size ?: 0} apartments")
            val updatedApartments = viewModel.getAllApartments()
            Log.d(TAG, "observeApartments: Updating adapter with ${updatedApartments.size} apartments")
            adapter.apartments = updatedApartments
            adapter.notifyDataSetChanged()
        }
    }

    // Navigates to expanded apartment view when an apartment is clicked.
    override fun setupApartmentsAdapterListener(): OnItemClickListener {
        return object: OnItemClickListener {
            override fun onItemClick(apartmentId: Int) {
                Log.d(TAG, "ApartmentsRecyclerAdapter: apartment id is $apartmentId")
                val apartment = viewModel.apartments?.value?.get(apartmentId)
                apartment?.let {
                    findNavController().navigate(R.id.action_apartmentsFragment_to_expandedApartmentFragment, bundleOf("apartmentId" to apartment.id))
                }
            }
        }
    }
}