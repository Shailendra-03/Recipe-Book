package com.codingsp.recipebook.repositories

import android.content.Intent
import androidx.core.net.toUri
import com.codingsp.recipebook.model.User
import com.codingsp.recipebook.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val storageRef = Firebase.storage.reference
    private val collection = db.collection("Users")


    suspend fun signInWithGmail(intentData: Intent): Resource<User> {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
        return try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Resource.Error(e.message + "")
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): Resource<User> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            addUserToDatabase(authResult.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message + "")
        }
    }

    private suspend fun addUserToDatabase(currentUser: FirebaseUser): Resource<User> {
        try {
            val user = User(
                currentUser.uid,
                currentUser.displayName!!,
                currentUser.email!!.substringBefore("@"),
                "",
                currentUser.email!!,
                currentUser.photoUrl.toString(),
                "",
                "",
                arrayListOf(),
                arrayListOf(),
                arrayListOf()
            )
            collection.document(currentUser.uid).set(user).await()
            return Resource.Success(user)
        } catch (e: Exception) {
            return Resource.Error(e.message + "")
        }
    }

    suspend fun sendResetPasswordEmail(email: String): String? {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            return e.message
        }
        return "A password reset link has been sent to your Email"
    }

    suspend fun createUserWithEmailAndPassword(user: User, userPassword: String): Resource<User> {
        return try {
            val authResult =
                auth.createUserWithEmailAndPassword(user.userEmail, userPassword).await()
            addUserImageToFirebaseStorage(authResult.user!!, user)
        } catch (e: Exception) {
            Resource.Error(e.message + "")
        }

    }

    private suspend fun addUserImageToFirebaseStorage(
        currentUser: FirebaseUser,
        user: User
    ): Resource<User> {
        try {
            val childRef = storageRef.child(currentUser.uid)
            if (user.userImageUrl.isNotEmpty()) {
                val imageRef =
                    childRef.child(user.userImageUrl.substringAfterLast('/') + System.currentTimeMillis())
                imageRef.putFile(user.userImageUrl.toUri()).await()
                val url = imageRef.downloadUrl.await()
                user.userImageUrl = url.toString()
            }
            if (user.userBackgroundImageUrl.isNotEmpty()) {
                val imageRef =
                    childRef.child(user.userBackgroundImageUrl.substringAfterLast('/') + System.currentTimeMillis())
                imageRef.putFile(user.userBackgroundImageUrl.toUri()).await()
                val url = imageRef.downloadUrl.await()
                user.userBackgroundImageUrl = url.toString()
            }

            return addUserToDatabase(currentUser, user)
        } catch (e: Exception) {
            return Resource.Error(e.message + "")
        }
    }

    private suspend fun addUserToDatabase(currentUser: FirebaseUser, user: User): Resource<User> {
        return try {
            user.userUUID = currentUser.uid
            collection.document(currentUser.uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message + "")
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<User> {
        return try {
            val currentUser = auth.signInWithEmailAndPassword(email, password).await()
            val user =
                FirebaseFirestore.getInstance().collection("Users").document(currentUser.user!!.uid)
                    .get().await().toObject(User::class.java)!!
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message + "")
        }
    }
}