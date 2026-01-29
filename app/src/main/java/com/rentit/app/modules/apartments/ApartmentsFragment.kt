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


class ApartmentsFragment : BaseApartmentsFragment() {
    private var TAG = "ApartmentsFragment"

    private val onEditClick: (apartmentId: String) -> Unit = {
        findNavController().navigate(R.id.action_apartmentsFragment_to_editApartmentFragment, bundleOf("apartmentId" to it))
    }

    override suspend fun preparations() {
        return UserModel.instance.getMe()
    }

    override fun setupApartmentsAdapter(): ApartmentsRecyclerAdapter {
        return ApartmentsRecyclerAdapter(viewModel.getAllApartments(), viewModel, onEditClick)
    }

    override fun observeApartments() {
        viewModel.apartments?.observe(viewLifecycleOwner) {
            progressBar.visibility = View.VISIBLE
            adapter.apartments = viewModel.getAllApartments()
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }
    }

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