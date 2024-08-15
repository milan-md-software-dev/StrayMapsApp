package com.example.straymaps.ui.screens.nearby_vet_clinics

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.straymaps.R
import com.example.straymaps.misc.bitmapFromDrawableRes
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mapbox.common.location.LocationObserver
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions


private const val TAG = "Nearby Vet Clinics Screen"

/** Composable function that gets the user's current location, and then displays a map with
 *  markers placed on nearby vet clinics and animal shelters
 */
@OptIn(MapboxExperimental::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun NearbyVetClinics(
    onBackClick: () -> Unit,
    viewModel: NearbyVetClinicsScreenViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val context = LocalContext.current

    var currentLocation: Point? by rememberSaveable { mutableStateOf(null) }

    var locationFound by rememberSaveable { mutableStateOf(false) }

    val locationProvider by viewModel.userLocation.collectAsState()

    var points: List<Point> by rememberSaveable { mutableStateOf(emptyList()) }

    val iconBitmap: Bitmap? = remember {bitmapFromDrawableRes(context, R.drawable.veterinary)}



    Log.e(TAG, "Location provider is $locationProvider")

    LaunchedEffect(locationProvider) {
        locationProvider?.let { provider ->
            if (multiplePermissionsState.allPermissionsGranted) {
                val locationObserver =
                    LocationObserver { locations ->
                        Log.i(TAG, "Location update received: $locations")
                    }
                provider.addLocationObserver(locationObserver)
                provider.getLastLocation { result ->
                    result?.let {
                        currentLocation = Point.fromLngLat(it.longitude,it.latitude)
                        Log.e(TAG, "Current location is: $currentLocation")
                    }
                }
                locationFound = true
            }
        }
    }


    //Currently Mapbox 11.0 does not support Search using Jetpack Compose
    //This functionality will be enabled in the future
    /**

    val discover = Discover.create("MAPBOX_DOWNLOADS_TOKEN")

    val query = DiscoverQuery.Category.create("vet_clinic")

    LaunchedEffect(currentLocation) {
    val response = currentLocation?.let { discover.search(query, it) }

    if (response != null) {
    response.onValue { results: List<DiscoverResult> ->
    {/**TO DO*/}
    }.onError { e: Exception ->
    Log.e(TAG, "Error finding nearby vet clinics.", e)
    }
    }
    }
     */

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.nearby_vet_clinics),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.arrow_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        PermissionHandling(
            multiplePermissionsState = multiplePermissionsState,
            onPermissionGranted = {
                if (currentLocation == null && !locationFound){
                    ProgressIndicatorWhileLocationIsFound(padding = innerPadding)
                } else if (currentLocation != null && locationFound) {
                    MapView(
                        innerPadding = innerPadding,
                        currentLocation = currentLocation,
                        points = points,
                        iconBitmap = iconBitmap)
                }
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandling(
    multiplePermissionsState: MultiplePermissionsState,
    onPermissionGranted: @Composable () -> Unit
){
    val locationPermissionText = if (multiplePermissionsState.shouldShowRationale) {
        stringResource(id = R.string.feature_requires_location)
    } else {
        stringResource(id = R.string.both_location_permissions_denied)
    }

    if (multiplePermissionsState.allPermissionsGranted) {
        onPermissionGranted()
    } else {
        LocationPermissionDialog(
            multiplePermissionsState = multiplePermissionsState,
            rationaleText = locationPermissionText,
            onPermissionRequest = { multiplePermissionsState.launchMultiplePermissionRequest() },
            actionText = stringResource(id = R.string.request_permissions)
        )
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun MapView(
    innerPadding: PaddingValues,
    currentLocation: Point?,
    points: List<Point>,
    iconBitmap: Bitmap?
){
    MapboxMap(
        Modifier
            .fillMaxSize()
            .padding(innerPadding),
        mapViewportState = remember {
            MapViewportState().apply {
                setCameraOptions {
                    zoom(10.0)
                    center(currentLocation)
                    pitch(0.0)
                    bearing(0.0)
                }
            }
        },
        style = {
            MapStyle(style = Style.SATELLITE_STREETS)
        }
    ) {
        PointAnnotationGroup(
            annotations = points.map {
                PointAnnotationOptions()
                    .withPoint(it)
                    .withIconImage(iconBitmap!!)
            },
            annotationConfig = AnnotationConfig(
                annotationSourceOptions = AnnotationSourceOptions(
                    clusterOptions = ClusterOptions(
                        textColorExpression = Expression.color(Color.BLUE),
                        textColor = Color.BLACK,
                        textSize = 20.0,
                        circleRadiusExpression = literal(25.0),
                        colorLevels = listOf(
                            Pair(100, Color.RED),
                            Pair(50, Color.BLUE),
                            Pair(0, Color.GREEN)
                        )
                    )
                )
            ),
        )
    }
}

@Composable
fun ProgressIndicatorWhileLocationIsFound(padding: PaddingValues){
    Box (
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.circular_progression_indicator_large)),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionDialog(
    multiplePermissionsState: MultiplePermissionsState,
    rationaleText: String,
    onPermissionRequest: () -> Unit,
    actionText: String
) {
    Card(
        elevation = CardDefaults.cardElevation(dimensionResource(id = R.dimen.card_elevation_small))
    ){
        Box(
            Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            contentAlignment = Alignment.Center
        ) {
            if (multiplePermissionsState.allPermissionsGranted) {
                Text(
                    stringResource(id = R.string.location_permissions_granted),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = rationaleText, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                    Button(onClick = onPermissionRequest,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        Text(text = actionText, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}




