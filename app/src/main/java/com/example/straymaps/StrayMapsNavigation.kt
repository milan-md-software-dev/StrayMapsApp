package com.example.straymaps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.StrayMapsAppState
import com.example.straymaps.ui.screens.edit_screen.EditPetScreen
import com.example.straymaps.ui.screens.edit_screen.EditStrayScreen
import com.example.straymaps.ui.screens.feed_a_stray.FeedAStrayScreen
import com.example.straymaps.ui.screens.home.StrayMapsHomeScreen
import com.example.straymaps.ui.screens.nearby_vet_clinics.NearbyVetClinics
import com.example.straymaps.ui.screens.pets_filed_reports.LostPetFiledReportScreen
import com.example.straymaps.ui.screens.report_a_lost_pet.ReportALostPet
import com.example.straymaps.ui.screens.report_a_stray.StrayReportScreen
import com.example.straymaps.ui.screens.sign_in.SignInScreenWithTopAppBar
import com.example.straymaps.ui.screens.sign_up.SignUpScreenWithTopAppBar
import com.example.straymaps.ui.screens.splash.SplashScreen
import com.example.straymaps.ui.screens.stray_filed_reports.StrayAnimalFiledReportScreen
import com.example.straymaps.ui.screens.welcome.WelcomeScreen


//Navigation composable function for StrayMaps app
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayMapsApp(
){
    val appState = rememberAppState()

    Scaffold{
        NavHost(
            navController = appState.navController,
            startDestination = StrayMapsScreen.SplashScreen.route,
            modifier = Modifier.padding(it)
        ) {
            strayMapsGraph(appState)
        }
    }
}

//NavController
@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController()) =
        remember(NavController) {
            StrayMapsAppState(navController)
        }


//Navigation graph builder
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.strayMapsGraph(
    appState: StrayMapsAppState
) {
    fun navigateToComposable(
        route: String,
        content: @Composable () -> Unit
    ) {
        composable(route) { content() }
    }

    //Splash screen is the first screen that shows up, to check whether the user is signed in
    navigateToComposable(StrayMapsScreen.SplashScreen.route){
        SplashScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }

    //If the user is not signed in, the splash screen leads them to the Welcome screen
        //which lets them create a new account, sign in, or continue as a guest
    navigateToComposable(StrayMapsScreen.Welcome.route){
        WelcomeScreen(
            showSignUpPage = {appState.navigate(StrayMapsScreen.SignUp.route)},
            showSignInPage = {appState.navigate(StrayMapsScreen.SignIn.route)},
            signInAnonymously = { route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }

    //Sign up screen lets the user create a new account
    navigateToComposable(StrayMapsScreen.SignUp.route){
        SignUpScreenWithTopAppBar(
            onBackClick = {appState.navigate(StrayMapsScreen.Welcome.route)},
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }

    //Sign in screen lets the user sign up with an already existing account
    navigateToComposable(StrayMapsScreen.SignIn.route){
        SignInScreenWithTopAppBar(
            onBackClick = {appState.popUp()},
            skipSignIn = {route -> appState.navigate(route)},
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
        )
    }

    //Home screen is the main "crossroads" screen of the app, where the user can pick several options
        //Also, this is where they can control their account
    navigateToComposable(StrayMapsScreen.Home.route){
        StrayMapsHomeScreen(
            onStraySpotterButtonClicked = {
                appState.navigate(StrayMapsScreen.ReportAStrayAnimal.route)
            },
            onLostPetsButtonClicked = {
                appState.navigate(StrayMapsScreen.ReportALostPet.route)
            },
            onStraySelection = {
                appState.navigate(StrayMapsScreen.SeeStrayFiledReports.route)
            },
            onPetSelection = {
                appState.navigate(StrayMapsScreen.SeePetFiledReports.route)
            },
            onWhereToGoButtonClicked = {
                appState.navigate(StrayMapsScreen.LookForNearbyAnimalSheltersAndVetClinics.route)
            },
            onFeedAStrayButtonClicked = {
                appState.navigate(StrayMapsScreen.DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome.route)
            },
            restartApp = { route -> appState.clearAndNavigate(route)
            },
            navigate = { route -> appState.navigate(route)
            },
            openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)
            }
        )
    }

    //Report a stray animal screen leads to the form that the user can fill out to report a stray
    navigateToComposable(StrayMapsScreen.ReportAStrayAnimal.route){
        StrayReportScreen(
            restartApp = {route -> appState.clearAndNavigate(route)},
            onBackClick = { appState.popUp() }
        )
    }

    //Report a lost pet screen leads to the form that the user can fill out to report their lost pet
    navigateToComposable(StrayMapsScreen.ReportALostPet.route){
        ReportALostPet(
            restartApp = {route -> appState.clearAndNavigate(route)},
            onBackClick = { appState.popUp() }
        )
    }

    //See stray filed reports leads the user to the screen where they can see the reports other users made about stray animals they saw
    navigateToComposable(StrayMapsScreen.SeeStrayFiledReports.route){
        StrayAnimalFiledReportScreen(
            restartApp = {route -> appState.clearAndNavigate(route)},
            onBackClick = { appState.navigate(StrayMapsScreen.Home.route) },
            navController = appState.navController
        )
    }

    //See pet filed reports leads the user to the screen where they can see the reports other users made about their lost pets
    navigateToComposable(StrayMapsScreen.SeePetFiledReports.route){
        LostPetFiledReportScreen(
            restartApp = {route -> appState.clearAndNavigate(route)},
            onBackClick = { appState.navigate(StrayMapsScreen.Home.route) },
            navController = appState.navController
        )
    }


    //Edit stray report screen lets the user edit reports they made themselves about stray animals
    composable(route = StrayMapsScreen.EditStrayReportScreen.route + "/{reportId}",
        arguments = listOf(navArgument("reportId"){type = NavType.IntType})
        ){
        EditStrayScreen(
            onBackClick = { appState.popUp() }
        )
    }

    //Edit lost pet reports lets the user edit reports they made themselves about their lost pets
    composable(route = StrayMapsScreen.EditPetReportScreen.route + "/{reportId}",
        arguments = listOf(navArgument("reportId"){type = NavType.IntType})
    ) {
        EditPetScreen(
            onBackClick = { appState.popUp() })
    }

    //Look for nearby animal shelters and vet clinics leads the user the Mapbox screen that automatically discovers
        //their location on the map and [WORK IN PROGRESS] shows nearby vet clinics and animal shelters
    navigateToComposable(StrayMapsScreen.LookForNearbyAnimalSheltersAndVetClinics.route){
        NearbyVetClinics(
            onBackClick = { appState.popUp() }
        )
    }

    //This screen shows information about animal care-related organisations and how users can donate money
    navigateToComposable(StrayMapsScreen.DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome.route){
        FeedAStrayScreen()

    }
}
