package com.codingsp.recipebook.viewmodel.fragmentViewModel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingsp.recipebook.model.Recipe
import com.codingsp.recipebook.repositories.RecipeRepository
import com.codingsp.recipebook.utils.AddRecipeState
import com.codingsp.recipebook.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPostViewModel : ViewModel() {
    private lateinit var currentPhotoPath: String
    private val recipeRepository = RecipeRepository()
    private var _imagesList = MutableLiveData<ArrayList<String>>()
    val imagesList: LiveData<ArrayList<String>> get() = _imagesList

    private var _isUploaded = MutableSharedFlow<Boolean>()
    val isUploaded = _isUploaded.asSharedFlow()

    private var _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private var _isProgressBarVisible = MutableSharedFlow<Boolean>()
    val isProgressBarVisible = _isProgressBarVisible.asSharedFlow()

    fun getImagesFromGallery(data: Intent) {
        val tempList: ArrayList<String> = arrayListOf()
        val clipData = data.clipData
        var i = 0;
        if (clipData != null) {
            while (i < clipData.itemCount) {
                tempList.add(clipData.getItemAt(i).uri.toString())
                i++;
            }
        } else {
            data.data?.let { tempList.add(it.toString()) }
        }
        _imagesList.value = tempList
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun saveImageUrisToList(image: ArrayList<String>) {
        _imagesList.value = image
    }

    fun checkForDataCorrectness(recipe: Recipe): AddRecipeState {
        when {
            recipe.recipeImage.isEmpty() -> {
                return AddRecipeState.RecipeImageError("Please add atleast one image")
            }
            recipe.recipeName.isEmpty() -> {
                return AddRecipeState.RecipeNameError("Please add Recipe title")
            }
            recipe.recipeDescription.isEmpty() -> {
                return AddRecipeState.RecipeDescriptionError("Please add some description")
            }
            recipe.ingredients.isEmpty() -> {
                return AddRecipeState.RecipeIngredientsError("Please add ingredients used in the recipe")
            }
            recipe.avgTime.isEmpty() -> {
                return AddRecipeState.RecipeTimeError("Please add average time required in minutes")
            }
            recipe.noOfServing.isEmpty() -> {
                return AddRecipeState.RecipeNoOfServingError("Please add No. of Serving")
            }
            recipe.instructions.isEmpty() -> {
                return AddRecipeState.RecipeInstructionsError("Please add instructions of the Recipe")
            }
            else -> {
                return AddRecipeState.Success()
            }
        }
    }

    fun addRecipeToDB(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main){ _isProgressBarVisible.emit(true) }
            val dataState = recipeRepository.addRecipeToDB(recipe)
            withContext(Dispatchers.Main) {
                observeAddRecipe(dataState)
            }
        }
    }

    private suspend fun observeAddRecipe(dataState: Resource<Boolean>) {
        _isProgressBarVisible.emit(false)
        when (dataState) {
            is Resource.Success -> {
                _isUploaded.emit(true)
            }
            is Resource.Error -> {
                _message.emit(dataState.message.toString())
            }
        }
    }

}