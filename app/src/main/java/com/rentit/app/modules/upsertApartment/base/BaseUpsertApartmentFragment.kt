package com.rentit.app.modules.upsertApartment.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import com.squareup.picasso.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.navigation.findNavController

// Shared fragment for both "Add" and "Edit" apartment flows
abstract class BaseUpsertApartmentFragment(val TAG: String) : Fragment() { // TAG is used for log messages
    private var startDate: Calendar = Calendar.getInstance() // Start date state for the sublet
    private var endDate: Calendar = Calendar.getInstance() // End date state for the sublet
    private var imageUri: Uri? = null // Holds the selected image URI (or parsed URL for edit)
    private val types = arrayOf("Apartment", "House", "Villa", "Penthouse") // Available apartment type options

    private lateinit var textTextView: TextView // Title/header text view (Add/Edit)
    private lateinit var titleTextField: EditText // Input for apartment title
    private lateinit var descriptionTextField: EditText // Input for apartment description
    private lateinit var roomsTextField: EditText // Input for number of rooms
    private lateinit var priceTextField: EditText // Input for price per night
    private lateinit var locationSelectField: Spinner // Spinner for selecting location/region
    private lateinit var typeSelectField: Spinner // Spinner for selecting type
    private lateinit var datesTextView: TextView // Displays selected date range
    private lateinit var datesBtn: ImageButton // Button to open date picker
    private lateinit var addImageBtn: ImageButton // Button to select (and show) apartment image
    private lateinit var uploadApartmentBtn: Button // Button to submit add/edit
    private lateinit var progressBar: ProgressBar // Shows loading state (edit fetch)
    private lateinit var backButton: ImageButton // Back button (edit flow)
    private lateinit var layout: View // Root content layout for toggling visibility

    private lateinit var _binding: FragmentBaseUpsertApartmentBinding // ViewBinding for this fragment layout
    private val binding get() = _binding // Convenience accessor for binding
    private lateinit var viewModel: BaseUpsertApartmentViewModel // ViewModel for fetching apartment in edit mode

    // ActivityResult launcher for picking an image from gallery
    private val addImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> // Register callback for picker result
        if (result.resultCode == Activity.RESULT_OK) { // Only handle successful selections
            val selectedImageUri = result.data?.data // Get the returned image URI (may be null)

            addImageBtn.setImageURI(selectedImageUri) // Preview selected image in the UI
            imageUri = selectedImageUri // Persist selected image URI for upload/update
        }
    }

    // Hook for subclasses (Edit) to supply the apartment id
    protected open fun getApartmentId(): String? = null // Default null means "add" flow

    // Inflate view and decide whether to load apartment (edit) or setup empty UI (add)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, // Inflater + parent container
        savedInstanceState: Bundle? // Saved state
    ): View {
        _binding = FragmentBaseUpsertApartmentBinding.inflate(inflater, container, false) // Inflate layout with ViewBinding
        viewModel = ViewModelProvider(this)[BaseUpsertApartmentViewModel::class.java] // Create ViewModel instance
        progressBar = binding.pbUpsertApartment // Bind progress bar from layout
        layout = binding.clUpsertApartmentLayout // Bind main layout container

        val apartmentId = getApartmentId() // Ask subclass if this is an edit flow
        if (apartmentId != null) { // Edit flow if id exists
            viewModel.setApartment(apartmentId) // Trigger ViewModel to load apartment by id

            viewModel.apartment?.observe(viewLifecycleOwner) { // Observe apartment data changes
                progressBar.visibility = View.VISIBLE // Show loader while preparing UI
                layout.visibility = View.GONE // Hide form content while loading
                lifecycleScope.launch { // Switch to coroutine for any async UI setup work
                    setupUi(it) // Populate UI with existing apartment data
                    progressBar.visibility = View.GONE // Hide loader after setup
                    layout.visibility = View.VISIBLE // Show populated content
                }
            }
        } else { // Add flow (no id)
            setupUi() // Setup UI with empty fields
            progressBar.visibility = View.GONE // Hide loader (no need to fetch)
            layout.visibility = View.VISIBLE // Show the form
        }

        return binding.root // Return the inflated fragment root view
    }

    @SuppressLint("SetTextI18n") // Suppress string concatenation lint warning for setting dates text
    protected fun setupUi(apartment : Apartment? = null) { // Optional apartment means edit mode when provided
        textTextView = binding.tvUpsertApartmentText // Bind header label
        titleTextField = binding.etUpsertApartmentTitle // Bind title input
        descriptionTextField = binding.etUpsertApartmentDescription // Bind description input
        roomsTextField = binding.etUpsertApartmentRooms // Bind rooms input
        priceTextField = binding.etUpsertApartmentPrice // Bind price input
        locationSelectField = binding.spUpsertApartmentLocation // Bind location spinner
        typeSelectField = binding.spUpsertApartmentType // Bind type spinner
        datesTextView = binding.tvUpsertApartmentDates // Bind dates display
        datesBtn = binding.ibUpsertApartmentDates // Bind dates picker button
        addImageBtn = binding.ibUpsertApartmentAddPhotoButton // Bind add-photo button
        uploadApartmentBtn = binding.btnUpsertApartmentUpload // Bind submit button
        backButton = binding.ibUpsertFragmentBack // Bind back button (used in edit)

        val locationAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, RegionsSingelton.regionsList) // Adapter for locations list
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Dropdown row layout
        locationSelectField.adapter = locationAdapter // Attach adapter to location spinner

        val typeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, types) // Adapter for type list
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Dropdown row layout
        typeSelectField.adapter = typeAdapter // Attach adapter to type spinner

        datesBtn.setOnClickListener(::onDatesButtonClicked) // Wire click to open date picker flow
        addImageBtn.setOnClickListener(::onAddImageButtonClicked) // Wire click to open image picker
        backButton.setOnClickListener(::onBackButtonClicked) // Wire click to navigate back

        if (apartment != null) { // Edit flow (populate UI)
            Log.d(TAG, "apartment: $apartment") // Log apartment object for debugging
            textTextView.text = "Edit Your Sublet" // Set header text for edit mode
            uploadApartmentBtn.text = "Edit Sublet" // Set button text for edit mode
            titleTextField.setText(apartment.title) // Fill title
            descriptionTextField.setText(apartment.description) // Fill description
            roomsTextField.setText(apartment.numOfRooms.toString()) // Fill rooms
            priceTextField.setText(apartment.pricePerNight.toString()) // Fill price
            locationSelectField.setSelection(locationAdapter.getPosition(apartment.city)) // Select matching location
            typeSelectField.setSelection(typeAdapter.getPosition(apartment.type.toString())) // Select matching type
            datesTextView.text = "${DateUtils.formatDate(apartment.startDate)} - ${DateUtils.formatDate(apartment.endDate)}" // Show saved date range
            imageUri = Uri.parse(apartment.imageUrl) // Store current image as URI (parsed from URL string)
            startDate.timeInMillis = apartment.startDate // Initialize start date state
            endDate.timeInMillis = apartment.endDate // Initialize end date state

            Picasso.get() // Get Picasso singleton
                .load(apartment.imageUrl) // Load image from URL
                .into(object : Target { // Use Target to receive bitmap callbacks
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) { // Called when bitmap is ready
                        addImageBtn.setImageBitmap(bitmap) // Show bitmap in addImageBtn
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) { // Called on failure
                        Log.e(TAG, e.toString()) // Log the error for debugging
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) { // Called right before load starts
                        Log.d(TAG, "onPrepareLoad") // Log load start
                    }
                })
            uploadApartmentBtn.setOnClickListener { onUpsertApartmentButtonClicked(it, apartment) } // Submit updates (edit)
        } else { // Add flow (empty UI)
            backButton.visibility = View.GONE // Hide back button in add mode
            uploadApartmentBtn.setOnClickListener(::onUpsertApartmentButtonClicked) // Submit new apartment (add)
        }
    }

    // Handles clicking the dates button (starts date selection flow)
    private fun onDatesButtonClicked(view: View) { // Click handler signature required by setOnClickListener
        setupDatePicker(view, startDate, true, endDate) // Pick start then end date sequentially
    }

    // Handles clicking the add image button (opens gallery picker)
    private fun onAddImageButtonClicked(view: View) { // Click handler signature required by setOnClickListener
        val imagePickerIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // Intent to pick image from external content

        addImageLauncher.launch(imagePickerIntent) // Launch picker and wait for result callback
    }

    @SuppressLint("SetTextI18n") // Suppress lint for string concatenation in dates UI
    private fun setupDatePicker(view: View, date: Calendar, anotherDatePicker: Boolean = false, anotherDate: Calendar? = null) { // Shared date picker logic
        val year = date.get(Calendar.YEAR) // Initial year shown in picker
        val month = date.get(Calendar.MONTH) // Initial month shown in picker
        val day = date.get(Calendar.DAY_OF_MONTH) // Initial day shown in picker

        val datePickerDialog = DatePickerDialog( // Create date picker dialog
            view.context, // Context from the clicked view
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth -> // Callback when a date is chosen
                date.set(year, monthOfYear, dayOfMonth) // Update the provided Calendar instance
                if (anotherDatePicker) { // If we need to pick another date after this one
                    val anotherDateSaver = anotherDate ?: return@OnDateSetListener
                    setupDatePicker(view, anotherDateSaver) // Open next picker (end date) using non-null asserted calendar
                } else { // Done choosing both dates
                    datesTextView.text = "${DateUtils.formatDate(startDate.timeInMillis)} - ${
                        DateUtils.formatDate(endDate.timeInMillis)
                    }" // Update UI with formatted date range
                }
            },
            year, // Initial picker year
            month, // Initial picker month
            day // Initial picker day
        )

        datePickerDialog.show() // Show the dialog to the user
    }

    // Handles creating/updating apartment after validation
    private fun onUpsertApartmentButtonClicked(view: View, apartment: Apartment? = null) { // apartment != null means edit mode
        val isValidTitle = RequiredValidation.validateRequiredTextField(titleTextField, "title") // Validate title is filled
        val isValidDescription = RequiredValidation.validateRequiredTextField(descriptionTextField, "description") // Validate description
        val isValidRooms = RequiredValidation.validateRequiredTextField(roomsTextField, "rooms") // Validate rooms
        val isValidPrice = RequiredValidation.validateRequiredTextField(priceTextField, "price") // Validate price
        val isValidPhoto = imageUri != null // Validate image is selected (or exists in edit)

        if (isValidTitle && isValidDescription && isValidRooms && isValidPrice && isValidPhoto) { // Proceed only if all inputs are valid
            lifecycleScope.launch(Dispatchers.IO) { // Do IO work (network/storage/db) off main thread
                try { // Guard all upsert work
                    val title = titleTextField.text.toString() // Read title input
                    val userId = AuthModel.instance.getUserId() ?: return@launch // Get current user id (non-null expected)\
                    val imageUriSaver = imageUri ?: return@launch // Get current image URI (non-null expected)
                    val description = descriptionTextField.text.toString() // Read description input
                    val numOfRooms = roomsTextField.text.toString().toInt() // Parse rooms count
                    val price = priceTextField.text.toString().toInt() // Parse price
                    val type = typeSelectField.selectedItem.toString() // Read selected type string
                    val location = locationSelectField.selectedItem.toString() // Read selected location string

                    if (apartment != null) { // Edit flow: update existing object
                        apartment.title = title // Update title
                        apartment.description = description // Update description
                        apartment.numOfRooms = numOfRooms // Update rooms
                        apartment.pricePerNight = price // Update price
                        apartment.type = Type.valueOf(type) // Update type (enum) from string
                        apartment.city = location // Update city/location
                        apartment.startDate = startDate.timeInMillis // Update start date
                        apartment.endDate = endDate.timeInMillis // Update end date

                        if (imageUri != Uri.parse(apartment.imageUrl)) { // Only re-upload if image was changed
                            val imageUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(imageUriSaver, FirebaseStorageModel.APARTMENTS_PATH) // Upload to Firebase Storage
                            apartment.imageUrl = imageUrl // Save new image URL
                        }

                        ApartmentModel.instance.updateApartment(apartment) // Persist updated apartment
                    } else { // Add flow: create new apartment object
                        val imageUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(imageUriSaver, FirebaseStorageModel.APARTMENTS_PATH) // Upload selected image
                        val newApartment = Apartment("", userId, title, price, description, location, Type.valueOf(type), numOfRooms, startDate.timeInMillis, endDate.timeInMillis, imageUrl) // Build new apartment model
                        ApartmentModel.instance.addApartment(newApartment) // Persist new apartment
                    }

                    lifecycleScope.launch(Dispatchers.Main) { // Switch back to main thread for UI + navigation
                        if (apartment == null) { // Add success path
                            Toast.makeText(
                                MyApplication.Globals.appContext, // App context
                                "sublet uploaded successfully", // Success message
                                Toast.LENGTH_SHORT, // Duration
                            ).show() // Show toast
                            Navigation.findNavController(view).popBackStack(R.id.apartmentsFragment, false) // Navigate back to apartments list
                        } else { // Edit success path
                            Toast.makeText(
                                MyApplication.Globals.appContext, // App context
                                "sublet updated successfully", // Success message
                                Toast.LENGTH_SHORT, // Duration
                            ).show() // Show toast
                            Navigation.findNavController(view).popBackStack() // Go back one screen
                        }
                    }
                } catch (e: Exception) { // Catch any unexpected exception from parsing/upload/db
                    Log.e(TAG, "An unexpected error occurred: ${e.message}") // Log error message
                    Toast.makeText(
                        MyApplication.Globals.appContext, // App context
                        if (apartment == null) "failed to upload sublet" else "failed to update sublet", // Failure message based on mode
                        Toast.LENGTH_SHORT, // Duration
                    ).show() // Show toast (note: this is called on IO dispatcher as-is)
                }
            }
        } else { // Validation failed path
            Log.e(TAG, "some of the apartment details are missing") // Log missing fields
            Toast.makeText(
                MyApplication.Globals.appContext, // App context
                "missing some sublet details", // User-facing message
                Toast.LENGTH_SHORT, // Duration
            ).show() // Show toast
        }
    }

    // Handles back button click (navigates to apartments fragment)
    private fun onBackButtonClicked(view: View) { // Click handler signature required by setOnClickListener
        Navigation.findNavController(view).navigate(R.id.apartmentsFragment) // Navigate to apartments fragment
    }
}