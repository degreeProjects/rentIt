package com.rentit.app.modules.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.rentit.app.R
import com.rentit.app.utils.RequiredValidation
import com.rentit.app.databinding.FragmentLoginBinding
import com.rentit.app.models.auth.AuthModel
import com.rentit.app.modules.register.RegisterFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LoginFragment
 *
 * Handles user authentication and login functionality.
 * Validates credentials and navigates to main app on successful login.
 */
class LoginFragment : Fragment() {
    companion object {
        const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null

    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoginBinding.inflate(inflater, container, false) // connects Kotlin code to the XML layout
        _binding = binding
        checkIfUserAuthenticated() // skip login if user already authenticated

        setupUi()

        return binding.root
    }

    // checks if user is already logged in and redirects to main app if authenticated
    private fun checkIfUserAuthenticated() {
        if (AuthModel.instance.getUser() != null) {
            findNavController().navigate(R.id.action_loginFragment_to_appActivity)
        }
    }

    // initializes UI components and sets up event listeners
    private fun setupUi() {
        val binding = _binding ?: return
        emailTextField = binding.etLoginFragmentEmail
        passwordTextField = binding.etLoginFragmentPassword
        loginButton = binding.btnLoginFragment
        signUpButton = binding.btnLoginFragmentSignUp

        loginButton.setOnClickListener(::onLoginButtonClicked)
        signUpButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registerFragment))
    }

    // validates input fields and attempts to sign in user with firebase authentication
    private fun onLoginButtonClicked(view: View) {
        val isValidEmail = RequiredValidation.validateRequiredTextField(emailTextField, "email")
        val isValidPassword = RequiredValidation.validateRequiredTextField(passwordTextField, "password")
        if (isValidEmail && isValidPassword) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // attempt authentication with firebase
                    AuthModel.instance.signIn(emailTextField.text.toString(), passwordTextField.text.toString())

                    // navigate to main app on successful login
                    withContext(Dispatchers.Main) {
                        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_appActivity)
                    }
                } catch (e: Exception) {
                    Log.e(RegisterFragment.TAG, "An unexpected error occurred: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "some of the login details are incorrect",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        } else {
            Log.e(TAG, "some of the login details are missing")
            Toast.makeText(
                requireContext(),
                "missing some login details",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}