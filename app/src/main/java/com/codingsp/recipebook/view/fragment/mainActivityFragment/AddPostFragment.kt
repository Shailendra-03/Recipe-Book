package com.codingsp.recipebook.view.fragment.mainActivityFragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codingsp.recipebook.BuildConfig
import com.codingsp.recipebook.R
import com.codingsp.recipebook.adapters.DishImagesViewPagerAdapter
import com.codingsp.recipebook.databinding.DialogImagePickBinding
import com.codingsp.recipebook.databinding.FragmentAddPostBinding
import com.codingsp.recipebook.databinding.ItemProgressLayoutBinding
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.utils.AddRecipeState
import com.codingsp.recipebook.utils.PermissionUtility
import com.codingsp.recipebook.viewmodel.fragmentViewModel.AddPostViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.lang.Exception

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var viewModel: AddPostViewModel
    private var category: String? = null
    private lateinit var adapter: DishImagesViewPagerAdapter
    private var imagesList: ArrayList<String>? = null
    private lateinit var auth: FirebaseAuth
    private var customProgressDialog : Dialog ?= null

    private var imageUri: Uri? = null


    private val registerLauncherForImagePick =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i("InAdapter", result.data.toString())
                if (result.data != null) {
                    viewModel.getImagesFromGallery(result.data!!)
                }
            }
        }

    private val launcherTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                if (imageUri != null) {
                    viewModel.saveImageUrisToList(arrayListOf(imageUri!!.toString()))
                }
            }
        }

    private val launcherForCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                getImagesFromCamera()
            } else {
                showAlertDialogOnPermissionDenied()
            }
        }
    private val launcherForGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                getImagesFromGallery()
            } else {
                showAlertDialogOnPermissionDenied()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
        imagesList = arrayListOf()
        auth = FirebaseAuth.getInstance()
        subscribeObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.RecipeCategory,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    category = adapterView?.getItemAtPosition(pos).toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
        binding.ibAddImage.setOnClickListener { customDialogForImagePick() }
        binding.ivDefaultImage.setOnClickListener { customDialogForImagePick() }

        adapter = DishImagesViewPagerAdapter(this.requireContext())
        binding.vpDishImages.adapter = adapter

        TabLayoutMediator(binding.tabLayoutVpDishImages, binding.vpDishImages) { _, _ -> }.attach()

        viewModel.imagesList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.ivDefaultImage.visibility = View.VISIBLE
                binding.ibAddImage.visibility = View.GONE
            } else {
                binding.ivDefaultImage.visibility = View.GONE
                binding.ibAddImage.visibility = View.VISIBLE
            }
            imagesList = it
            adapter.setList(it)
        }
    }

    private fun getImagesFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        registerLauncherForImagePick.launch(intent)
    }

    private fun customDialogForImagePick() {
        val customDialog = Dialog(this.requireContext())
        val dialogBinding = DialogImagePickBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        dialogBinding.llUsingCamera.setOnClickListener {
            if (PermissionUtility.hasCameraPermission(requireContext())) {
                getImagesFromCamera()
            } else {
                launcherForCameraPermission.launch(android.Manifest.permission.CAMERA)
            }
            customDialog.dismiss()
        }
        dialogBinding.llUsingGallery.setOnClickListener {
            if (PermissionUtility.hasGalleryPermission(requireContext())) {
                getImagesFromGallery()
            } else {
                launcherForGalleryPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            customDialog.dismiss()
        }
        customDialog.setCanceledOnTouchOutside(true)
        customDialog.show()
    }

    private fun getImagesFromCamera() {
        val photoFile: File? = try {
            viewModel.createImageFile(requireContext())
        } catch (e: Exception) {
            null
        }
        photoFile?.also {
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider", it
            )
            launcherTakePicture.launch(imageUri)
        }
    }

    private fun showAlertDialogOnPermissionDenied() {
        AlertDialog.Builder(requireContext())
            .setMessage("We need the permission to get the image for your recipe.You can grant the permissions in the app settings")
            .setPositiveButton(
                "Go To Settings"
            ) { dialogInterface, _ ->
                goToAppSettings()
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun goToAppSettings() {
        try {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.isUploaded.collectLatest {
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        "Recipe Uploaded Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(R.id.action_navigationAddPost_to_navigation_home)
                }else {
                    customProgressDialog?.dismiss()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.message.collectLatest {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isProgressBarVisible.collectLatest {
                if(it) {
                    showCustomProgressDialog()
                }else{
                    customProgressDialog?.dismiss()
                }
            }
        }
    }

    private fun showCustomProgressDialog() {
        customProgressDialog = Dialog(this.requireContext())
        val dialogBinding = ItemProgressLayoutBinding.inflate(layoutInflater)
        customProgressDialog!!.setContentView(dialogBinding.root)
        customProgressDialog!!.setCanceledOnTouchOutside(false)
        customProgressDialog!!.show()
    }

    private fun checkForCorrectness() {
        val createdAt = System.currentTimeMillis().toString()
        val recipeName = binding.etRecipeName.text.toString().trim()
        val recipeDescription = binding.etRecipeDescription.text.toString().trim()
        val ingredients = binding.etIngredients.text.toString().trim()
        val avgTime = binding.etAvgTime.text.toString().trim()
        val noOfServing = binding.etNoOfServing.text.toString().trim()
        val dishCategory = category!!
        val instructions = binding.etInstruction.text.toString().trim()

        val recipe = Recipe(
            createdAt, imagesList!!, recipeName, recipeDescription, ingredients,
            avgTime, noOfServing, dishCategory,
            instructions, arrayListOf(), recipeId = "", auth.currentUser!!.uid
        )
        removeAllEditTextErrors()
        binding.tilAvgTime.isErrorEnabled = false
        when (val errorState = viewModel.checkForDataCorrectness(recipe)) {
            is AddRecipeState.RecipeImageError -> {
                Snackbar.make(binding.root, errorState.message + "", Snackbar.LENGTH_LONG).show()
            }
            is AddRecipeState.RecipeDescriptionError -> {
                binding.tilRecipeDescription.error = errorState.message
                binding.tilRecipeDescription.requestFocus()
            }
            is AddRecipeState.RecipeIngredientsError -> {
                binding.tilIngredients.error = errorState.message
                binding.tilIngredients.requestFocus()
            }
            is AddRecipeState.RecipeInstructionsError -> {
                binding.tilInstruction.error = errorState.message
                binding.tilInstruction.requestFocus()
            }
            is AddRecipeState.RecipeNameError -> {
                binding.tilRecipeName.error = errorState.message
                binding.tilRecipeName.requestFocus()
            }
            is AddRecipeState.RecipeNoOfServingError -> {
                binding.tilNoOfServing.error = errorState.message
                binding.tilNoOfServing.requestFocus()
            }
            is AddRecipeState.RecipeTimeError -> {
                binding.tilAvgTime.error = errorState.message
                binding.tilAvgTime.requestFocus()
            }
            is AddRecipeState.Success -> {
                viewModel.addRecipeToDB(recipe)
            }
        }
    }

    private fun removeAllEditTextErrors() {
        binding.tilAvgTime.isErrorEnabled = false
        binding.tilInstruction.isErrorEnabled = false
        binding.tilRecipeName.isErrorEnabled = false
        binding.tilIngredients.isErrorEnabled = false
        binding.tilNoOfServing.isErrorEnabled = false
        binding.tilAvgTime.isErrorEnabled = false
        binding.tilRecipeDescription.isErrorEnabled = false
    }
}