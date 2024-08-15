package com.example.straymaps.ui.screens.home

import com.example.straymaps.data.User
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StrayMapsHomeScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    ) : StrayMapsViewModel() {

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    //When the screen is first opened, calls on getCurrentUserInfo method
    init {
        getCurrentUserInfo()
    }

    /** This method checks whether the user is logged in
     *  if not, it resets the screen to SplashScreen, which then leads the user to Welcome Screen where they can sign in or sign up
     */
    fun initialize(restartApp: (String) -> Unit){
        launchCatching {
            accountService.currentUser.collect{user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    //Method that gets the current user's profile info
    private fun getCurrentUserInfo() {
        launchCatching {
            _currentUserProfile.value = accountService.getUserProfile()
        }
    }

    fun onSignInClick(navigate:(String) -> Unit) {
        navigate(StrayMapsScreen.SignIn.route)
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(StrayMapsScreen.SignUp.route, StrayMapsScreen.Home.route)
    }

    //Function that lets the user sign out
    fun onSignOutClick(){
        launchCatching {
            accountService.signOut()
        }
    }

    //Function that lets the user delete their account
    fun onDeleteAccountClick(){
        launchCatching {
            accountService.deleteAccount()
        }
    }

}