package com.codingsp.recipebook.model

data class UserStories(
    val userId:String="",
    var stories:ArrayList<Story> = arrayListOf()
)
