package com.rentit.app.modules.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.rentit.app.R
import com.rentit.app.models.FirebaseStorageModel
import com.rentit.app.utils.RequiredValidation
import com.rentit.app.databinding.FragmentRegisterBinding
import com.rentit.app.models.auth.AuthModel
import com.rentit.app.models.user.User
import com.rentit.app.models.user.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.Navigation
import androidx.navigation.findNavController

/**
 * RegisterFragment
 *
 * Handles new user registration with Firebase authentication.
 * Collects user details including name, email, password, phone number, and avatar.
 */
class RegisterFragment : Fragment() {
    companion object {
        const val TAG = "RegisterFragment"
    }

    private var _binding: FragmentRegisterBinding? = null

    private lateinit var userImageView: ImageView
    private lateinit var nameTextField: EditText
    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var phoneNumberTextField: EditText
    private lateinit var addImageBtn: Button
    private lateinit var registerButton: Button
    private lateinit var signInButton: Button
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var layout: View
    private var avatarUri: Uri? = null

    // handles avatar image selection from device gallery
    private val addImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data

            userImageView.setImageURI(selectedImageUri)
            avatarUri = selectedImageUri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false) // connects Kotlin code to the XML layout
        _binding = binding
        setupUi()

        return binding.root
    }

    // initializes UI components and sets up event listeners
    private fun setupUi() {
        val binding = _binding ?: return
        layout = binding.clRegisterFragment
        progressBar = binding.progressBarRegisterFragment
        userImageView = binding.ivRegisterFragmentUserAvatar
        nameTextField = binding.etRegisterFragmentName
        emailTextField = binding.etRegisterFragmentEmail
        passwordTextField = binding.etRegisterFragmentPassword
        phoneNumberTextField = binding.etRegisterFragmentPhoneNumber
        addImageBtn = binding.btnRegisterFragmentAddImage
        registerButton = binding.btnRegisterFragment
        signInButton = binding.btnRegisterFragmentSignIn

        addImageBtn.setOnClickListener { onAddImageButtonClicked() }
        registerButton.setOnClickListener(::onRegisterButtonClicked)
        signInButton.setOnClickListener(::onSignInButtonClicked)
    }

    // opens device gallery to select an avatar image
    private fun onAddImageButtonClicked() {
        val imagePickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        addImageLauncher.launch(imagePickerIntent)
    }

    // validates all input fields and creates new user account with firebase authentication
    private fun onRegisterButtonClicked(view: View) {
        val isValidName = RequiredValidation.validateRequiredTextField(nameTextField, "name")
        val isValidEmail = RequiredValidation.validateRequiredTextField(emailTextField, "email")
        val isValidPassword = RequiredValidation.validateRequiredTextField(passwordTextField, "password")
        val isValidPhoneNumber = RequiredValidation.validateRequiredTextField(phoneNumberTextField, "phone number")
        val isValidPhoto = avatarUri != null

        if (isValidName && isValidEmail && isValidPassword && isValidPhoneNumber && isValidPhoto) {
            // Show loading spinner
            progressBar.visibility = View.VISIBLE
            layout.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // create firebase authentication account
                    val authResult = AuthModel.instance.signUp(emailTextField.text.toString(), passwordTextField.text.toString())
                    val userId = authResult.user?.uid ?: return@launch
                    val currentAvatarUri = avatarUri ?: return@launch
                    // upload avatar to firebase storage
                    val avatarUrl = FirebaseStorageModel.instance.addImageToFirebaseStorage(currentAvatarUri, FirebaseStorageModel.USERS_PATH)
                    // create user document in firestore
                    val user = User(userId, nameTextField.text.toString(),phoneNumberTextField.text.toString(), emailTextField.text.toString(), avatarUrl)
                    UserModel.instance.addUser(user)
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE // Hide loading spinner
                        layout.visibility = View.VISIBLE
                        Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_appActivity)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "An unexpected error occurred: ${e.message}")
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        layout.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            e.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        } else {
            Log.e(TAG, "some of the registration details are missing")
            Toast.makeText(
                requireContext(),
                "missing some registration details",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // navigates back to login screen
    private fun onSignInButtonClicked(view: View) {
        view.findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}