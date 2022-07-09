package com.codingsp.recipebook.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val createdAt:String="",
    var recipeImage:ArrayList<String> = arrayListOf(),
    val recipeName:String="",
    val recipeDescription:String="",
    val ingredients:String="",
    val avgTime:String="",
    val noOfServing:String="",
    val dishCategory:String="",
    val instructions:String="",
    val likedBy:ArrayList<String> = arrayListOf(),
    var recipeId:String="",
    var createdBy:String="",
) : Parcelable