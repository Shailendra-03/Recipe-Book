package com.codingsp.recipebook.view.fragment.loginActivityFragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.codingsp.recipebook.databinding.FragmentSignUpBinding
import com.codingsp.recipebook.databinding.ItemProgressLayoutBinding
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.utils.UserDetailsState
import com.codingsp.recipebook.view.activity.MainActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.SignUpViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: FragmentSignUpBinding
    private var userImageUri: Uri? = null
    private var userBackgroundImageUri: Uri? = null
    private var loginProgressDialog : Dialog ?=null
    private lateinit var cal: Calendar
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var registerLauncherForImagePick =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataIntent: Intent? = result.data
                dataIntent?.data?.let {
                    userImageUri = it
                    binding.ivUserSignUpFragment.setImageURI(userImageUri)
                }
            }
        }
    private var registerLauncherForBackgroundImagePick =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataIntent: Intent? = result.data
                dataIntent?.data?.let {
                    userBackgroundImageUri = it
                    binding.ivUserBackgroundSignUpFragment.setImageURI(userBackgroundImageUri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        cal = Calendar.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener(this)
        binding.flIvUserSignUpFragment.setOnClickListener(this)
        binding.etDateOfBirth.setOnClickListener(this)
        binding.flIvBackgroundSignUpFragment.setOnClickListener(this)
        binding.ivBackSignUpFragment.setOnClickListener(this)

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.isProgressDialogVisible.collectLatest {
                if(it){
                    showCustomProgressDialog()
                }else{
                    loginProgressDialog?.dismiss()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.signedInUser.collectLatest {
                val intent = Intent(requireContext(),MainActivity::class.java)
                intent.putExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY,it)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        viewModel.errorSignupMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Toast.makeText(this.context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.ivBackSignUpFragment -> {
                requireActivity().onBackPressed()
            }
            binding.flIvUserSignUpFragment -> {
                getUserImage()
            }
            binding.flIvBackgroundSignUpFragment -> {
                getUserBackground()
            }
            binding.etDateOfBirth -> {
                val datePickerDialog = DatePickerDialog(
                    requireContext(), dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()
            }
            binding.btnRegister -> {
                val userName = binding.etUsername.text.toString().trim { it <= ' ' }
                val name = binding.etName.text.toString().trim { it <= ' ' }
                val dateOfBirth = binding.etDateOfBirth.text?.trim { it <= ' ' }
                val userEmail = binding.etEmail.text.toString().trim { it <= ' ' }
                val userPassword = binding.etPassword.text.toString().trim { it <= ' ' }
                val userBio = binding.etBioSignUpFragment.text.toString().trim()
                val imageUri = when {
                    userImageUri != null -> {
                        userImageUri.toString()
                    }
                    else -> {
                        ""
                    }
                }
                val backgroundUri = when {
                    userBackgroundImageUri != null -> {
                        userBackgroundImageUri.toString()
                    }
                    else -> {
                        ""
                    }
                }
                val user = User(
                    "", name, userName, dateOfBirth.toString(), userEmail,
                    imageUri, userBio, backgroundUri,
                    arrayListOf(), arrayListOf(), arrayListOf()
                )
                removeAllEditTextErrors()
                createUser(
                    viewModel.checkForDetails(userName, user, userPassword),
                    user,
                    userPassword
                )
            }
        }
    }

    private fun removeAllEditTextErrors() {
        binding.tilUsername.isErrorEnabled = false
        binding.tilName.isErrorEnabled = false
        binding.tilEmail.isErrorEnabled = false
        binding.tilDateOfBirth.isErrorEnabled = false
        binding.tilPassword.isErrorEnabled = false
    }

    private fun createUser(userDetailsState: UserDetailsState, user: User, userPassword: String) {
        when(userDetailsState){
            is UserDetailsState.UserProfileImageError ->{
                Snackbar.make(binding.root,userDetailsState.message+"",Snackbar.LENGTH_LONG).show()
            }
            is UserDetailsState.UserDOBError -> {
                binding.tilDateOfBirth.error = userDetailsState.message
                binding.tilDateOfBirth.requestFocus()
            }
            is UserDetailsState.UserDisplayNameError -> {
                binding.tilName.error = userDetailsState.message
                binding.tilName.requestFocus()
            }
            is UserDetailsState.UserEmailError -> {
                binding.tilEmail.error = userDetailsState.message
                binding.tilEmail.requestFocus()
            }
            is UserDetailsState.UserNameError -> {
                binding.tilUsername.error = userDetailsState.message
                binding.tilUsername.requestFocus()
            }
            is UserDetailsState.UserPasswordError -> {
                binding.tilPassword.error = userDetailsState.message
                binding.tilPassword.requestFocus()
            }
            is UserDetailsState.Success -> {
                viewModel.createUserWithEmailAndPassword(user,userPassword)
            }
        }
    }

    private fun getUserImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        registerLauncherForImagePick.launch(intent)
    }

    private fun getUserBackground() {
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        registerLauncherForBackgroundImagePick.launch(intent)
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDateOfBirth.setText(sdf.format(cal.time).toString())
    }

    private fun showCustomProgressDialog() {
        loginProgressDialog = Dialog(this.requireContext())
        val dialogBinding = ItemProgressLayoutBinding.inflate(layoutInflater)
        dialogBinding.progressMessage.text = "Logging In..."
        loginProgressDialog!!.setContentView(dialogBinding.root)
        loginProgressDialog!!.setCanceledOnTouchOutside(false)
        loginProgressDialog!!.show()
    }


}