package com.rentit.app.modules.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rentit.app.MainActivity
import com.rentit.app.R
import com.rentit.app.base.MyApplication
import com.rentit.app.utils.RequiredValidation
import com.rentit.app.databinding.FragmentProfileBinding
import com.rentit.app.models.auth.AuthModel
import com.rentit.app.models.user.UpdateUserInput
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class ProfileFragment : Fragment() {
    companion object {
        private const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private lateinit var viewModel: ProfileViewModel

    private lateinit var avatarImageButton: ImageButton
    private lateinit var nameTextField: EditText
    private lateinit var phoneNumberTextField: EditText
    private lateinit var editButton: Button
    private lateinit var logoutButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: View
    private var avatarUri: Uri? = null
    private var avatarTarget: Target? = null

    private val addImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data

            avatarImageButton.setImageURI(selectedImageUri)
            avatarUri = selectedImageUri
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        _binding = binding

        setupUi()

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        viewModel.getCurrentUser()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            // Show UI immediately - don't wait for avatar to load
            progressBar.visibility = View.GONE
            layout.visibility = View.VISIBLE

            nameTextField.setText(user.name)
            phoneNumberTextField.setText(user.phoneNumber)
                        
            // Clear avatar URI after successful update
            avatarUri = null

            // Load avatar image - will be instant from cache due to preloading
            if (!user.avatarUrl.isNullOrEmpty()) {
                // Hide avatar while loading to avoid showing placeholder
                avatarImageButton.alpha = 0f
                
                // Create Target to control visibility
                val target = object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        Log.d(TAG, "Avatar loaded from: $from")
                        avatarImageButton.setImageBitmap(bitmap)
                        // Fade in smoothly
                        avatarImageButton.animate().alpha(1f).setDuration(150).start()
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        Log.e(TAG, "Failed to load avatar: ${e?.message}")
                        avatarImageButton.setImageResource(R.drawable.account_circle)
                        avatarImageButton.animate().alpha(1f).setDuration(150).start()
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // Keep hidden while loading
                        Log.d(TAG, "Preparing to load avatar")
                    }
                }
                avatarTarget = target
                
                Picasso.get()
                    .load(user.avatarUrl)
                    .error(R.drawable.account_circle)
                    .into(target)
            } else {
                // If avatarUrl is null or empty, just show the placeholder
                avatarImageButton.setImageResource(R.drawable.account_circle)
                avatarImageButton.alpha = 1f
            }
        }

        return binding.root
    }

    private fun setupUi() {
        val binding = _binding ?: return
        layout = binding.clProfileFragment
        avatarImageButton = binding.ivbProfileFragmentAvatar
        nameTextField = binding.etProfileFragmentName
        phoneNumberTextField = binding.etProfileFragmentPhoneNumber
        editButton = binding.btnProfileFragmentEdit
        logoutButton = binding.fabProfileFragmentLogout
        progressBar = binding.progressBarProfileFragment

        avatarImageButton.setOnClickListener(::onImageViewButton)
        editButton.setOnClickListener(::onEditButtonClicked)
        logoutButton.setOnClickListener(::onLogoutButtonClicked)
    }

    private fun onEditButtonClicked(view: View) {
        val isValidName = RequiredValidation.validateRequiredTextField(nameTextField, "name")
        val isValidEmail = RequiredValidation.validateRequiredTextField(phoneNumberTextField, "phone number")

        if (isValidName && isValidEmail) {
            val avatarUrl =  viewModel.user.value?.avatarUrl ?: return
            val name = nameTextField.text.toString()
            val phoneNumber = phoneNumberTextField.text.toString()

            // Show loading spinner
            progressBar.visibility = View.VISIBLE
            layout.visibility = View.GONE

            val updateUserInput = UpdateUserInput(name, phoneNumber, avatarUrl)
            viewModel.updateCurrentUser(updateUserInput, avatarUri)
            Toast.makeText(
                requireContext(),
                "details updated",
                Toast.LENGTH_SHORT,
            ).show()
        } else {
            Log.e(TAG, "missing some user details")
            Toast.makeText(
                requireContext(),
                "you must fill all the fields",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun onImageViewButton(view: View) {
        val imagePickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        addImageLauncher.launch(imagePickerIntent)
    }

    private fun onLogoutButtonClicked(view: View) {
        AuthModel.instance.signOut()
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clear the target to prevent memory leaks
        avatarTarget = null
        _binding = null
    }
}
