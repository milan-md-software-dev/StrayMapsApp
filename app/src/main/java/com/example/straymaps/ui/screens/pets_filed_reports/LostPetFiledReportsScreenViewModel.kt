package com.example.straymaps.ui.screens.pets_filed_reports

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.data.repositories.OfflineLostPetRepository
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
class LostPetFiledReportsScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val lostPetRepository: OfflineLostPetRepository
): StrayMapsViewModel() {

    private val _allLostPetFiledReportsState: MutableStateFlow<List<LostPet>> =
        MutableStateFlow(emptyList())
    val allLostPetFiledReportState: StateFlow<List<LostPet>> =
        _allLostPetFiledReportsState.asStateFlow()

    private var _microchipIdReportFound = MutableStateFlow<Boolean?>(null)
    var microchipIdReportFound: StateFlow<Boolean?> = _microchipIdReportFound.asStateFlow()

    var userInputMicrochipId by mutableStateOf("")

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    //Calls on initialize method, which loads all the filed reports and gets the current user info
    init {
        _currentUserId.value = accountService.currentUserId
    }

    fun initialize(restartApp: (String) -> Unit){
        getAllLostPetFiledReportState()
        observeAuthenticationState(restartApp)
    }

    //Checks if the user is signed in
    private fun observeAuthenticationState(restartApp: (String) -> Unit){
        launchCatching {
            accountService.currentUser.collect{user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    private fun getAllLostPetFiledReportState(){
        viewModelScope.launch {
            lostPetRepository.loadAllLostPetReports().collect { reports ->
                withContext(Dispatchers.Main) {
                    _allLostPetFiledReportsState.value = reports
                }
            }
        }
    }

    /** This part deals with sorting the reports shown in different ways
     *
     */
    private fun getAllLostPetReportsByCriteria(loadReports: suspend() -> Flow<List<LostPet>>) {
        viewModelScope.launch {
            loadReports().collect { reports ->
                withContext(Dispatchers.Main) {
                    _allLostPetFiledReportsState.value = reports
                }
            }
        }
    }

    fun getAllLostPetReportsByType(){
        getAllLostPetReportsByCriteria {
            lostPetRepository.loadAllByType()
        }
    }

    fun getAllLostPetReportsByColour(){
        getAllLostPetReportsByCriteria {
            lostPetRepository.loadAllByColour()
        }
    }

    fun getAllLostPetReportsBySex(){
        getAllLostPetReportsByCriteria {
            lostPetRepository.loadAllBySex()
        }
    }

    fun getAllLostPetReportsByDate(){
        getAllLostPetReportsByCriteria {
            lostPetRepository.loadAllByDateAndTime()
        }
    }

    //Using the input by user (i.e. microchip ID), tries to find a matching report
    fun findLostPetReportByMicrochipId(input: String) {
        viewModelScope.launch{
            val report = lostPetRepository.getLostPetsByMicrochipId(input.uppercase(Locale.getDefault()))
            if (report != null) {
                _microchipIdReportFound.value = true
                _allLostPetFiledReportsState.value = listOf(report)
            } else {
                _microchipIdReportFound.value = false
            }
        }
    }

    fun resetMicrochipIdReportFoundValueToNull(){
        _microchipIdReportFound.value = null
    }

    //Need to implement functionality for specific type of animal search
    val foundAllLostPetOfSpecificType: Flow<List<LostPet>> = lostPetRepository.getAllLostPetsOfSpecificType(type = "dog")

    suspend fun upsertLostPet(lostPet: LostPet){
        viewModelScope.launch{
            lostPetRepository.upsertLostPet(lostPet)
        }
    }

    suspend fun deleteLostPet(lostPet: LostPet) {
        viewModelScope.launch{
            lostPetRepository.deleteLostPet(lostPet)
        }
    }

    //Method for showing the report's date and time in proper way
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String{
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy || HH:mm")
        return localDateTime.format(formatter)
    }

    fun updateUserInputMicrochipId(input: String){
        userInputMicrochipId = input
    }
}