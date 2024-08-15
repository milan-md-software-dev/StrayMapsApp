package com.example.straymaps.ui.screens.sign_in

import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class StrayMapsSignInScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    //Method that checks if the user is already logged in
    fun initialize(navigate: (String) -> Unit){
       launchCatching {
           if (accountService.hasUser() && accountService.getUserProfile().isAnonymous == false) navigate(StrayMapsScreen.Home.route)
       }
    }

    fun getEmail(newEmail: String) {
        email.value = newEmail
    }

    fun getPassword(newPassword: String) {
        password.value = newPassword
    }

    fun onSignInClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            accountService.signIn(email.value, password.value)
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SignIn.route)
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(StrayMapsScreen.SignUp.route, StrayMapsScreen.SignIn.route)
    }
}