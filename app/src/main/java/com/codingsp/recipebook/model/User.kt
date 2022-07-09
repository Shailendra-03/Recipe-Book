package com.codingsp.recipebook.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    var userUUID:String="",
    val userDisplayName:String="",
    val userName:String="",
    val userDOB:String="",
    val userEmail:String="",
    var userImageUrl:String="",
    var userBio:String="",
    var userBackgroundImageUrl:String="",
    val recipeList: ArrayList<String> = arrayListOf(),
    val followers:ArrayList<String> = arrayListOf(),
    val following:ArrayList<String> = arrayListOf()
):Parcelable