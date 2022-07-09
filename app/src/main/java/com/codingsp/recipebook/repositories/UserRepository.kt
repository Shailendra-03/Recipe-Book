package com.codingsp.recipebook.repositories

import com.codingsp.recipebook.daos.UserDao
import com.codingsp.recipebook.model.User
import java.util.ArrayList

class UserRepository {
    private val userDao = UserDao()
    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)
    }

    suspend fun updateUserFollowersData(userId: String, followers: ArrayList<String>) {
        userDao.updateUserFollowersData(userId, followers)
    }

    suspend fun updateUserFollowingData(userId: String, following: ArrayList<String>) {
        userDao.updateUserFollowingData(userId, following)
    }
}