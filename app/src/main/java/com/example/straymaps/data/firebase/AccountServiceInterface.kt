package com.example.straymaps.data.firebase

import com.example.straymaps.data.User
import kotlinx.coroutines.flow.Flow

//Interface for setting up Firebase Authentication
interface AccountServiceInterface {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean

    fun getUserProfile(): User
    suspend fun createAnonymousAccount()
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun linkAccount(email: String, password: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}