package com.example.straymaps.misc

object Constants {
    const val STRAY_MAPS_DATABASE = "Stray_Maps_Database"
}

//Sealed class used as Strings for routes in navigation composable (in StrayMapsNavigation.kt file)
sealed class StrayMapsScreen(val route: String){
    object SplashScreen: StrayMapsScreen("splash")
    object Welcome: StrayMapsScreen("welcome")
    object Home: StrayMapsScreen("home")
    object SignUp: StrayMapsScreen("sign_up")
    object SignIn: StrayMapsScreen("sign_in")
    object ReportAStrayAnimal: StrayMapsScreen("report_stray_animal")
    object ReportALostPet: StrayMapsScreen("report_lost_pet")
    object SeeStrayFiledReports: StrayMapsScreen("stray_filed_reports")
    object SeePetFiledReports: StrayMapsScreen("pet_filed_reports")
    object LookForNearbyAnimalSheltersAndVetClinics: StrayMapsScreen("nearby_shelters/vet_clinics")
    object DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome: StrayMapsScreen("donate")
    object EditStrayReportScreen: StrayMapsScreen("edit_stray")
    object EditPetReportScreen: StrayMapsScreen("edit_pet")
}