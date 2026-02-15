package com.rentit.app.modules.apartments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rentit.app.R
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.modules.apartments.ApartmentsViewModel

 // Interface for handling apartment item click events.
interface OnItemClickListener {
    fun onItemClick(apartmentId: Int)
}

/**
 * ApartmentsRecyclerAdapter
 * 
 * RecyclerView adapter for displaying a list of apartments.
 * Manages apartment item views and handles user interactions (clicks, likes, edits, deletes).
 * 
 * @param apartments List of apartments to display
 * @param viewModel ViewModel to handle business logic for apartment operations
 * @param onEditClick Callback function invoked when edit button is clicked
 */
class ApartmentsRecyclerAdapter(var apartments: List<Apartment>?, private val viewModel: ApartmentsViewModel, private val onEditClick: (apartmentId: String) -> Unit): RecyclerView.Adapter<ApartmentsViewHolder>() {
    // Listener for apartment item clicks
    var listener: OnItemClickListener? = null

    // creates a new ViewHolder for apartment items.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.apartment_list_row, parent, false)
        return ApartmentsViewHolder(itemView, this)
    }

    // returns the total number of apartments in the list.
    override fun getItemCount(): Int = apartments?.size ?: 0

     // binds apartment data to the ViewHolder at the specified position.
    override fun onBindViewHolder(holder: ApartmentsViewHolder, position: Int) {
        val apartment = apartments?.get(position)
        holder.bind(apartment)
    }

     // handles click events on apartment items.
    fun onClick(apartmentId: Int) {
        listener?.onItemClick(apartmentId)
    }

     // handles like/unlike button clicks.
    fun onLikeClick(position: Int) {
        val apartment = apartments?.get(position)

        apartment?.let {
            val liked = !apartment.liked
            viewModel.onLikeClick(apartment.id, liked)
        }
    }

    // handles edit button clicks.
    fun onEditClick(position: Int) {
        val apartment = apartments?.get(position)
        apartment?.let {
            onEditClick(it.id)
        }
    }

    // handles delete button clicks.
    fun onDeleteClick(position: Int) {
        val apartment = apartments?.get(position)
        apartment?.let {
            viewModel.onDeleteClick(it.id)
        }
    }
}