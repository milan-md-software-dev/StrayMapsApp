package com.example.straymaps.ui.screens.welcome

import android.util.Log
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


const val TAG = "StrayMapsWelcomeScreen"

@HiltViewModel
class StrayMapsWelcomeScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
): StrayMapsViewModel() {
    fun signInAnonymously(openAndPopUp: (String, String) -> Unit){
        launchCatching {
            accountService.createAnonymousAccount()
            Log.d(TAG, "Current anonymous userId is: ${accountService.currentUserId}")
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.Welcome.route)
        }
    }
}