package com.rentit.app.modules.expandedApartment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.rentit.app.R
import com.rentit.app.databinding.FragmentExpandedApartmentBinding
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.user.UserModel
import com.rentit.app.utils.DateUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ExpandedApartmentFragment : Fragment() {
    private lateinit var binding: FragmentExpandedApartmentBinding
    private lateinit var viewModel: ExpandedApartmentViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: View
    private lateinit var image: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var roomsTextView: TextView
    private lateinit var propertyTypeTextView: TextView
    private lateinit var datesTextView: TextView
    private lateinit var personTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var backButton: ImageButton

    private val args: ExpandedApartmentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpandedApartmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ExpandedApartmentViewModel::class.java]

        progressBar = binding.pbExpandedApartment
        layout = binding.clExpandedApartmentLayout
        titleTextView = binding.tvExpandedApartmentTitle
        descriptionTextView = binding.tvExpandedApartmentDescription
        priceTextView = binding.tvExpandedApartmentPrice
        locationTextView = binding.tvExpandedApartmentLocation
        roomsTextView = binding.tvExpandedApartmentRooms
        propertyTypeTextView = binding.tvExpandedApartmentType
        datesTextView = binding.tvExpandedApartmentDates
        personTextView = binding.tvExpandedApartmentPerson
        emailTextView = binding.tvExpandedApartmentEmail
        phoneTextView = binding.tvExpandedApartmentPhone
        image = binding.ivExpandedApartmentImage
        backButton = binding.ibExpandedFragmentBack

        backButton.setOnClickListener(::onBackButtonClicked)

        val apartmentId: String = args.apartmentId
        viewModel.setApartment(apartmentId)

        viewModel.apartment?.observe(viewLifecycleOwner) {
            progressBar.visibility = View.VISIBLE
            layout.visibility = View.GONE
            lifecycleScope.launch {
                bind(it)
                progressBar.visibility = View.GONE
                layout.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private suspend fun bind(apartment: Apartment) {
        titleTextView.text = apartment.title
        descriptionTextView.text = apartment.description
        priceTextView.text = "${apartment.pricePerNight}$"
        locationTextView.text = apartment.city
        roomsTextView.text = apartment.numOfRooms.toString()
        propertyTypeTextView.text = apartment.type.toString()
        datesTextView.text = "${DateUtils.formatDate(apartment.startDate)} - ${DateUtils.formatDate(apartment.endDate)}"

        val user = UserModel.instance.getUserById(apartment.userId)

        user?.let { u ->
            personTextView.text = u.name
            emailTextView.text = u.email
            phoneTextView.text = u.phoneNumber
        }

        // Load image with Picasso - no placeholder so image waits to display until fully loaded
        if (!apartment.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(apartment.imageUrl)
                .error(R.drawable.default_apartment)
                .into(image)
        } else {
            image.setImageResource(R.drawable.default_apartment)
        }

        progressBar.visibility = View.GONE
        layout.visibility = View.VISIBLE
    }

    private fun onBackButtonClicked(view: View) {
        Navigation.findNavController(view).navigate(R.id.apartmentsFragment)
    }
}