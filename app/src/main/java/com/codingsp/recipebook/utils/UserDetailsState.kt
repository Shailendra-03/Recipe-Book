package com.codingsp.recipebook.utils

sealed class UserDetailsState(val message: String ?=null){
    class Success :UserDetailsState()
    class UserProfileImageError(message: String ) :UserDetailsState(message)
    class UserNameError(message: String ) :UserDetailsState(message)
    class UserDisplayNameError(message: String ) :UserDetailsState(message)
    class UserEmailError(message: String ) :UserDetailsState(message)
    class UserDOBError(message: String ) :UserDetailsState(message)
    class UserPasswordError(message: String ) :UserDetailsState(message)
}
