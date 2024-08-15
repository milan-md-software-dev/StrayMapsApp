package com.example.straymaps.ui.screens.sign_up

import com.example.straymaps.data.User
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.IllegalArgumentException
import javax.inject.Inject

@HiltViewModel
class StrayMapsSignUpScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
): StrayMapsViewModel(){

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    //Method that calls on getCurrentUserInfo method when the screen is displayed
    init {
        getCurrentUserInfo()
    }

    val signUpEmail = MutableStateFlow("")
    val signUpPassword = MutableStateFlow("")
    val signUpConfirmPassword = MutableStateFlow("")

    //Gets the current user account information
    private fun getCurrentUserInfo() {
        launchCatching {
            _currentUserProfile.value = accountService.getUserProfile()
        }
    }

    fun updateEmail(email: String){
        signUpEmail.value = email
    }

    fun updatePassword(password: String){
        signUpPassword.value = password
    }

    fun updateConfirmPassword(newConfirmPassword: String){
        signUpConfirmPassword.value = newConfirmPassword
    }

    fun onSignUpClick(openAndPopUp :(String, String) -> Unit) {
        launchCatching {
            if (signUpPassword.value != signUpConfirmPassword.value) {
                throw IllegalArgumentException("Passwords do not match!")
            }

            accountService.signUp(signUpEmail.value, signUpPassword.value)
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SignIn.route)
        }
    }

    fun onSignUpAnonymousLinkClick(openAndPopUp :(String, String) -> Unit) {
        launchCatching {
            if (signUpPassword.value != signUpConfirmPassword.value) {
                throw IllegalArgumentException("Passwords do not match!")
            }

            accountService.linkAccount(signUpEmail.value, signUpPassword.value)
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SignIn.route)
        }
    }


}