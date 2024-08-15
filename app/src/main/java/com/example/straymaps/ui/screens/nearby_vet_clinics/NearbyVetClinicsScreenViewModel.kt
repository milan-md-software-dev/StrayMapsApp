package com.example.straymaps.ui.screens.nearby_vet_clinics


import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.straymaps.ui.screens.StrayMapsViewModel
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NearbyVetClinicsScreenViewModel @Inject constructor()
    : StrayMapsViewModel()
{

    private val _userLocation = MutableStateFlow<LocationProvider?>(null)
    val userLocation: StateFlow<LocationProvider?> = _userLocation.asStateFlow()

    //Calls on the getUserLocation method
    init {
        getUserLocation()
    }

    //Gets the user's current location
    private fun getUserLocation() {
        viewModelScope.launch(Dispatchers.Main) {
            val TAG = "getUserLocation"

            val locationService: LocationService = LocationServiceFactory.getOrCreate()
            var locationProvider: DeviceLocationProvider? = null

            val request = LocationProviderRequest.Builder()
                .interval(
                    IntervalSettings.Builder().interval(0L).minimumInterval(0L).maximumInterval(0L)
                        .build()
                )
                .displacement(0F)
                .accuracy(AccuracyLevel.HIGHEST)
                .build()

            val result = locationService.getDeviceLocationProvider(request)

            if (result.isValue) {
                locationProvider = result.value!!
            } else {
                Log.e(TAG, "Error getting user location.")
            }

            _userLocation.value = locationProvider
        }
    }

}