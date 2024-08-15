package com.example.straymaps.ui.screens.stray_filed_reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.data.repositories.OfflineStrayAnimalRepository
import com.example.straymaps.data.stray_animal.StrayAnimal
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class StrayAnimalFiledReportsViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val strayAnimalRepository: OfflineStrayAnimalRepository
): StrayMapsViewModel() {

    private val _allStrayAnimalFiledReportsState: MutableStateFlow<List<StrayAnimal>> =
        MutableStateFlow(emptyList())
    val allStrayAnimalFiledReportState: StateFlow<List<StrayAnimal>> =
        _allStrayAnimalFiledReportsState.asStateFlow()

    var userInputMicrochipID by mutableStateOf("")

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private var _microchipIdReportFound = MutableStateFlow<Boolean?>(null)
    var microchipIdReportFound: StateFlow<Boolean?> = _microchipIdReportFound.asStateFlow()

    //Calls on initialize method, which loads all the filed reports and gets the current user info
    init {
        _currentUserId.value = accountService.currentUserId
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(restartApp: (String) -> Unit) {
            getAllStrayAnimalReports()
            observeAuthenticationState(restartApp)
    }

    //Checks if the user is signed in
    private fun observeAuthenticationState(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect{user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllStrayAnimalReports(){
        viewModelScope.launch {
            strayAnimalRepository.loadAllStrayAnimalReports().collect { reports ->
                withContext(Dispatchers.Main) {
                    _allStrayAnimalFiledReportsState.value = reports
                }
            }
        }
    }

    /** This part deals with sorting the reports shown in different ways
     *
     */
    private fun getAllStrayAnimalReportsByCriteria (loadReports: suspend () -> Flow<List<StrayAnimal>>) {
        viewModelScope.launch {
            loadReports().collect { reports ->
                withContext(Dispatchers.Main) {
                    _allStrayAnimalFiledReportsState.value = reports
                }
            }
        }
    }

    fun getAllStrayReportsByType(){
        getAllStrayAnimalReportsByCriteria {
            strayAnimalRepository.loadAllByType()
        }
    }

    fun getAllStrayReportsByColour(){
        getAllStrayAnimalReportsByCriteria {
            strayAnimalRepository.loadAllByColour()
        }
    }

    fun getAllStrayReportsBySex(){
        getAllStrayAnimalReportsByCriteria {
            strayAnimalRepository.loadAllBySex()
        }
    }

    fun getAllStrayReportsByDate(){
        getAllStrayAnimalReportsByCriteria {
            strayAnimalRepository.loadAllByDateAndTime()
        }
    }

    //Using the input by user (i.e. microchip ID), tries to find a matching report
    fun findStrayAnimalReportByMicrochipId(input: String) {
        viewModelScope.launch {
            val report = strayAnimalRepository.getStrayAnimalByMicrochipId(input.uppercase(Locale.getDefault()))
            if (report != null) {
                _microchipIdReportFound.value = true
                _allStrayAnimalFiledReportsState.value = listOf(report)
            } else {
                _microchipIdReportFound.value = false
            }
        }
    }

        fun resetMicrochipIdReportFoundValueToNull(){
        _microchipIdReportFound.value = null
    }

    //Need to implement functionality for specific type of animal search
    val foundAllStrayAnimalOfSpecificType: Flow<List<StrayAnimal>> = strayAnimalRepository.getAllStrayAnimalOfSpecificType(type = "dog")


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun upsertStrayAnimal(strayAnimal: StrayAnimal) {
        viewModelScope.launch {
            strayAnimalRepository.upsertStrayAnimal(strayAnimal)
        }
    }

    suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal) {
        viewModelScope.launch {
            strayAnimalRepository.deleteStrayAnimal(strayAnimal)
        }
    }

    //Method for showing the report's date and time in proper way
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String{
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy | HH:mm")
        return localDateTime.format(formatter)
    }


    fun updateUserInputMicrochipId(input: String) {
        userInputMicrochipID = input
    }

}

