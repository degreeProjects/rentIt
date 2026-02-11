package com.rentit.app.modules.apartments.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rentit.app.R
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.utils.DateUtils
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class ApartmentsViewHolder(itemView: View, adapter: ApartmentsRecyclerAdapter): RecyclerView.ViewHolder(itemView) {
    private val TAG = "ApartmentsViewHolder"

    private var titleTextView: TextView
    private var priceTextView: TextView
    private var locationTextView: TextView
    private var roomsTextView: TextView
    private var propertyTypeTextView: TextView
    private var datesTextView: TextView

    private var actionsLayout: View
    private var image: ImageView
    private var likeButton: ImageButton
    private var editButton: ImageButton
    private var deleteButton: ImageButton

    init {
        titleTextView = itemView.findViewById(R.id.tvApartmentsListTitle)
        priceTextView = itemView.findViewById(R.id.tvApartmentsListPrice)
        locationTextView = itemView.findViewById(R.id.tvApartmentsListLocation)
        roomsTextView = itemView.findViewById(R.id.tvApartmentsListRooms)
        propertyTypeTextView = itemView.findViewById(R.id.tvApartmentsListPropertyType)
        datesTextView = itemView.findViewById(R.id.tvApartmentsListDates)
        actionsLayout = itemView.findViewById(R.id.clApartmentsListActions)
        image = itemView.findViewById(R.id.ivApartmentsListImage)
        likeButton = itemView.findViewById(R.id.ibApartmentsListLikeButton)
        editButton = itemView.findViewById(R.id.ibApartmentsListEditButton)
        deleteButton = itemView.findViewById(R.id.ibApartmentsListDeleteButton)

        itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                adapter.onClick(position)
            }
        }

        likeButton.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                adapter.onLikeClick(position)
                if (adapter.apartments?.get(position)?.liked == true) {
                    likeButton.setImageResource(R.drawable.like_button)
                } else {
                    likeButton.setImageResource(R.drawable.liked_like_button)
                }
            }
        }

        editButton.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Edit button clicked")
                adapter.onEditClick(position) // navigate to add post fragment with apartment data ?
            }
        }

        deleteButton.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                Log.d(TAG, "Delete button clicked")
                adapter.onDeleteClick(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(apartment: Apartment?) {
        titleTextView.text = apartment?.title
        priceTextView.text = "${apartment?.pricePerNight}$"
        locationTextView.text = apartment?.city
        roomsTextView.text = apartment?.numOfRooms.toString()
        propertyTypeTextView.text = apartment?.type.toString()
        datesTextView.text = "${DateUtils.formatDate(apartment?.startDate ?: 0)} - ${DateUtils.formatDate(apartment?.endDate ?: 0)}"

        // Only load image if URL is not null or empty
        if (!apartment?.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(apartment?.imageUrl)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        image.setImageBitmap(bitmap)
                    }
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        Log.e(TAG, e.toString())
                    }
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        Log.d(TAG, "onPrepareLoad")
                    }
                })
        } else {
            // Optionally set a placeholder image when URL is empty
            image.setImageResource(R.drawable.default_apartment)
        }

        if (apartment?.isMine == true) {
            likeButton.visibility = View.GONE
            actionsLayout.visibility = View.VISIBLE
        } else {
            likeButton.visibility = View.VISIBLE
            actionsLayout.visibility = View.GONE

            if (apartment?.liked == true) {
                likeButton.setImageResource(R.drawable.liked_like_button)
            }
        }
    }
}