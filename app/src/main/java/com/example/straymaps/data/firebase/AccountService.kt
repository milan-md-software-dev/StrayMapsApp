package com.example.straymaps.data.firebase

import com.example.straymaps.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Class for implementing Firebase Authentication
 *  Contains methods for checking if there is a current user, as well as
 *  getting the user profile, id, creating anonymous account (in case a user wants to try the app
 *  out before creating an actual account), signing in, signing out, etc
 */
class AccountService @Inject constructor(
    private val auth: FirebaseAuth
): AccountServiceInterface {

    override val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.toStrayMapsUser())
                }
            auth.addAuthStateListener(listener)
            awaitClose {auth.removeAuthStateListener(listener)}
        }

    override fun getUserProfile(): User {
        return auth.currentUser.toStrayMapsUser()
    }

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override fun hasUser(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun createAnonymousAccount() {
        auth.signInAnonymously().await()
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun linkAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    private fun FirebaseUser?.toStrayMapsUser(): User {
        return this?.let {
            User(
                id = uid,
                email = email.orEmpty(),
                provider = providerId,
                displayName = displayName.orEmpty(),
                isAnonymous = isAnonymous
            )
        } ?: User()
    }
}