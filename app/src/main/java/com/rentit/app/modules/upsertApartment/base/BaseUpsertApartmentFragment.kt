package com.rentit.app.modules.upsertApartment.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.rentit.app.R
import com.rentit.app.base.MyApplication
import com.rentit.app.models.FirebaseStorageModel
import com.rentit.app.utils.RequiredValidation
import com.rentit.app.databinding.FragmentBaseUpsertApartmentBinding
import com.rentit.app.models.apartment.Apartment
import com.rentit.app.models.apartment.ApartmentModel
import com.rentit.app.models.apartment.Type
import com.rentit.app.models.auth.AuthModel
import com.rentit.app.retrofit.RegionsSingelton
import com.rentit.app.utils.DateUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.navigation.findNavController

/**
 * BaseUpsertApartmentFragment
 *
 * Shared base fragment for adding and editing apartments.
 * Handles form UI, validation, image selection, and apartment creation/updates.
 */
abstract class BaseUpsertApartmentFragment(val TAG: String) : Fragment() {
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    private var imageUri: Uri? = null
    private val types = arrayOf("Apartment", "House", "Villa", "Penthouse")

    private lateinit var textTextView: TextView
    private lateinit var titleTextField: EditText
    private lateinit var descriptionTextField: EditText
    private lateinit var roomsTextField: EditText
    private lateinit var priceTextField: EditText
    private lateinit var locationSelectField: Spinner
    private lateinit var typeSelectField: Spinner
    private lateinit var datesTextView: TextView
    private lateinit var datesBtn: ImageButton
    private lateinit var addImageBtn: ImageButton
    private lateinit var uploadApartmentBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var layout: View

    private lateinit var _binding: FragmentBaseUpsertApartmentBinding
    private val binding get() = _binding
    private lateinit var viewModel: BaseUpsertApartmentViewModel

    // handles image selection from device gallery
    private val addImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data

            addImageBtn.setImageURI(selectedImageUri)
            imageUri = selectedImageUri
        }
    }

    // subclasses override this to provide apartment ID for edit mode
    protected open fun getApartmentId(): String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaseUpsertApartmentBinding.inflate(inflater, container, false) // connects Kotlin code to the XML layout
        viewModel = ViewModelProvider(this)[BaseUpsertApartmentViewModel::class.java] // connects the Fragment to its data source

        progressBar = binding.pbUpsertApartment
        layout = binding.clUpsertApartmentLayout

        val apartmentId = getApartmentId()
        if (apartmentId != null) {
            viewModel.setApartment(apartmentId)

            viewModel.apartment?.observe(viewLifecycleOwner) {
                progressBar.visibility = View.VISIBLE
                layout.visibility = View.GONE
                lifecycleScope.launch {
                    setupUi(it)
                    progressBar.visibility = View.GONE
                    layout.visibility = View.VISIBLE
                }
            }
        } else {
            setupUi()
            progressBar.visibility = View.GONE
            layout.visibility = View.VISIBLE
        }

        return binding.root
    }

    // initializes UI components and populates fields for edit mode if apartment is provided
    @SuppressLint("SetTextI18n")
    protected fun setupUi(apartment : Apartment? = null) {
        textTextView = binding.tvUpsertApartmentText
        titleTextField = binding.etUpsertApartmentTitle
        descriptionTextField = binding.etUpsertApartmentDescription
        roomsTextField = binding.etUpsertApartmentRooms
        priceTextField = binding.etUpsertApartmentPrice
        locationSelectField = binding.spUpsertApartmentLocation
        typeSelectField = binding.spUpsertApartmentType
        datesTextView = binding.tvUpsertApartmentDates
        datesBtn = binding.ibUpsertApartmentDates
        addImageBtn = binding.ibUpsertApartmentAddPhotoButton
        uploadApartmentBtn = binding.btnUpsertApartmentUpload
        backButton = binding.ibUpsertFragmentBack

        val locationAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, RegionsSingelton.regionsList) // get regions from API
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // set dropdown layout
        locationSelectField.adapter = locationAdapter

        val typeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, types) // set types
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        typeSelectField.adapter = typeAdapter

        // sets buttons listeners
        datesBtn.setOnClickListener(::onDatesButtonClicked)
        addImageBtn.setOnClickListener(::onAddImageButtonClicked)
        backButton.setOnClickListener(::onBackButtonClicked)

        if (apartment != null) {
            // Edit Apartment Mode
            Log.d(TAG, "apartment: $apartment")
            textTextView.text = "Edit Your Apartment"
            uploadApartmentBtn.text = "Edit"
            titleTextField.setText(apartment.title)
            descriptionTextField.setText(apartment.description)
            roomsTextField.setText(apartment.numOfRooms.toString())
            priceTextField.setText(apartment.pricePerNight.toString())
            locationSelectField.setSelection(locationAdapter.getPosition(apartment.city))
            typeSelectField.setSelection(typeAdapter.getPosition(apartment.type.toString()))
            datesTextView.text = "${DateUtils.formatDate(apartment.startDate)} - ${DateUtils.formatDate(apartment.endDate)}"
            imageUri = Uri.parse(apartment.imageUrl)
            startDate.timeInMillis = apartment.startDate
            endDate.timeInMillis = apartment.endDate

            if (!apartment.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(apartment.imageUrl)
                    .error(R.drawable.default_apartment)
                    .into(addImageBtn)
            } else {
                addImageBtn.setImageResource(R.drawable.default_apartment)
            }
            
            uploadApartmentBtn.setOnClickListener { onUpsertApartmentButtonClicked(it, apartment) }
        } else {
            // Add Apartment Mode
            backButton.visibility = View.GONE
            addImageBtn.setImageResource(R.drawable.default_apartment)
            uploadApartmentBtn.setOnClickListener(::onUpsertApartmentButtonClicked)
        }
    }

    // opens date picker dialogs to select start and end dates
    private fun onDatesButtonClicked(view: View) {
        setupDatePicker(view, startDate, true, endDate)
    }

    // opens device gallery to select apartment image
    private fun onAddImageButtonClicked(view: View) {
        val imagePickerIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        addImageLauncher.launch(imagePickerIntent)
    }

    // configures and displays date picker dialog
    @SuppressLint("SetTextI18n")
    private fun setupDatePicker(view: View, date: Calendar, anotherDatePicker: Boolean = false, anotherDate: Calendar? = null) {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            view.context,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                if (anotherDatePicker) {
                    val anotherDateSaver = anotherDate ?: return@OnDateSetListener
                    setupDatePicker(view, anotherDateSaver)
                } else {
                    datesTextView.text = "${DateUtils.formatDate(startDate.timeInMillis)} - ${
                        DateUtils.formatDate(endDate.timeInMillis)
                    }"
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    // validates form inputs and creates or updates apartment in firebase
    private fun onUpsertApartmentButtonClicked(view: View, apartment: Apartment? = null) {
        val isValidTitle = RequiredValidation.validateRequiredTextField(titleTextField, "title")
        val isValidDescription = RequiredValidation.validateRequiredTextField(descriptionTextField, "description")
        val isValidRooms = RequiredValidation.validateRequiredTextField(roomsTextField, "rooms")
        val isValidPrice = RequiredValidation.validateRequiredTextField(priceTextField, "price")

        if (isValidTitle && isValidDescription && isValidRooms && isValidPrice) {
            progressBar.visibility = View.VISIBLE
            layout.visibility = View.GONE
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val title = titleTextField.text.toString()
                    val userId = AuthModel.instance.getUserId() ?: return@launch
                    val description = descriptionTextField.text.toString()
                    val numOfRooms = roomsTextField.text.toString().toInt()
                    val price = priceTextField.text.toString().toInt()
                    val type = typeSelectField.selectedItem.toString()
                    val location = locationSelectField.selectedItem.toString()

                    if (apartment != null) {
                        // Update existing apartment

                        apartment.title = title
                        apartment.description = description
                        apartment.numOfRooms = numOfRooms
                        apartment.pricePerNight = price
                        apartment.type = Type.valueOf(type)
                        apartment.city = location
                        apartment.startDate = startDate.timeInMillis
                        apartment.endDate = endDate.timeInMillis

                        imageUri?.let { uri ->
                            if (uri != Uri.parse(apartment.imageUrl)) {
                                val imageUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(uri, FirebaseStorageModel.APARTMENTS_PATH)
                                apartment.imageUrl = imageUrl
                            }
                        }

                        ApartmentModel.instance.updateApartment(apartment)
                    } else {
                        // Add new apartment

                        val uploadUri = imageUri ?: Uri.parse(
                            "android.resource://${requireContext().packageName}/${R.drawable.default_apartment}"
                        )

                        // add the image to Firebase Storage and get the download URL
                        val imageUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(uploadUri, FirebaseStorageModel.APARTMENTS_PATH)
                        Log.d(TAG, "Image uploaded, URL: $imageUrl")
                        
                        if (imageUrl.isEmpty()) {
                            throw Exception("Failed to upload image to Firebase Storage")
                        }
                        
                        try {
                            Picasso.get().load(imageUrl).get() // pre-load the image into Picasso cache
                            Log.d(TAG, "Image pre-loaded into Picasso cache")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to pre-load image into cache: ${e.message}")
                        }
                        
                        val newApartment = Apartment("", userId, title, price, description, location, Type.valueOf(type), numOfRooms, startDate.timeInMillis, endDate.timeInMillis, imageUrl)
                        ApartmentModel.instance.addApartment(newApartment) // add the new apartment to the database
                    }

                    lifecycleScope.launch(Dispatchers.Main) {
                        if (apartment == null) {
                            // upload success
                            Toast.makeText(
                                requireContext(),
                                "apartment uploaded successfully",
                                Toast.LENGTH_SHORT,
                            ).show()
                            Navigation.findNavController(view).popBackStack(R.id.apartmentsFragment, false) // navigate back to apartments list
                        } else {
                            // update success
                            Toast.makeText(
                                requireContext(),
                                "apartment updated successfully",
                                Toast.LENGTH_SHORT,
                            ).show()
                            Navigation.findNavController(view).popBackStack() // navigate back to apartments list
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "An unexpected error occurred: ${e.message}")
                    lifecycleScope.launch(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        layout.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            if (apartment == null) "failed to upload apartment" else "failed to update apartment",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        } else {
            Log.e(TAG, "some of the apartment details are missing")
            Toast.makeText(
                requireContext(),
                "missing some apartment details",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // navigates back to apartments list
    private fun onBackButtonClicked(view: View) {
        Navigation.findNavController(view).navigate(R.id.apartmentsFragment)
    }
}
