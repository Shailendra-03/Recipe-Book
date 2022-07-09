package com.codingsp.recipebook.view.fragment.loginActivityFragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.marginStart
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.FragmentSignInBinding
import com.codingsp.recipebook.databinding.ItemProgressLayoutBinding
import com.codingsp.recipebook.utils.Constants
import com.codingsp.recipebook.view.activity.MainActivity
import com.codingsp.recipebook.viewmodel.fragmentViewModel.SignInViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest

class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: SignInViewModel
    private lateinit var binding: FragmentSignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var loginStateDialog : Dialog ?=null

    var resultLauncherForGmailSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intentData: Intent? = result.data
                intentData?.let {
                    viewModel.signInWithGmail(it)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SignInViewModel::class.java]
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSignUp.setOnClickListener(this)
        binding.ibGmail.setOnClickListener(this)
        binding.tvSignUp.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnSignIn.setOnClickListener(this)


        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) {
            if (it == true) {
                showCustomProgressDialog()
            } else {
                loginStateDialog?.dismiss()
            }
        }
        viewModel.message.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                Toast.makeText(this.context, it, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.signedInUser.collectLatest {
                val intent = Intent(requireContext(),MainActivity::class.java)
                intent.putExtra(Constants.USER_DETAILS_FROM_LOGIN_ACTIVITY_TO_MAIN_ACTIVITY,it)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }


    override fun onClick(view: View?) {
        when (view) {
            binding.btnSignIn -> {
                val email = binding.etUsername.text.toString().trim()
                val password = binding.etPassword.text.toString()
                if (viewModel.checkForEmailCorrectness(email) || password.isNotEmpty()) {
                    viewModel.signInWithEmailAndPassword(email, password)
                }
            }
            binding.tvSignUp -> {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }
            binding.ibGmail -> {
                signInWithGmail()
            }
            binding.tvForgotPassword -> {
                val email = binding.etUsername.text.toString().trim()
                if (viewModel.checkForEmailCorrectness(email)) {
                    viewModel.sendResetPasswordEmail(email)
                }
            }
        }
    }

    private fun signInWithGmail() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)

        val signInIntent = googleSignInClient.signInIntent
        resultLauncherForGmailSignIn.launch(signInIntent)
    }

    private fun showCustomProgressDialog() {
        loginStateDialog = Dialog(this.requireContext())
        val dialogBinding = ItemProgressLayoutBinding.inflate(layoutInflater)
        dialogBinding.progressMessage.text = "Logging In..."
        loginStateDialog!!.setContentView(dialogBinding.root)
        loginStateDialog!!.setCanceledOnTouchOutside(false)
        loginStateDialog!!.show()
    }


}