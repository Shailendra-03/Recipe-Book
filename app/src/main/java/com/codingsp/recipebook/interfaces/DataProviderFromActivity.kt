package com.codingsp.recipebook.interfaces

import com.codingsp.recipebook.model.User

interface DataProviderFromActivity {
    fun getUserFromActivity(): User?
}