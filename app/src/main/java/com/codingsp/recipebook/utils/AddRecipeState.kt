package com.codingsp.recipebook.utils

sealed class AddRecipeState(val message: String? = null){
    class Success():AddRecipeState()
    class RecipeImageError(message: String?= null) : AddRecipeState(message)
    class RecipeNameError(message: String? = null) : AddRecipeState(message)
    class RecipeDescriptionError(message: String? = null) : AddRecipeState(message)
    class RecipeIngredientsError(message: String? = null) : AddRecipeState(message)
    class RecipeTimeError(message: String? = null) : AddRecipeState(message)
    class RecipeNoOfServingError(message: String? = null) : AddRecipeState(message)
    class RecipeInstructionsError(message: String? = null) : AddRecipeState(message)
}
