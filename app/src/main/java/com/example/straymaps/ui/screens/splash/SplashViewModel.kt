package com.example.straymaps.ui.screens.splash

import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) :StrayMapsViewModel() {

    fun onAppStart(openAndPopUp: (String, String) -> Unit) {
        if (accountService.hasUser()) openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SplashScreen.route)
        else openAndPopUp(StrayMapsScreen.Welcome.route, StrayMapsScreen.SplashScreen.route)
    }
}