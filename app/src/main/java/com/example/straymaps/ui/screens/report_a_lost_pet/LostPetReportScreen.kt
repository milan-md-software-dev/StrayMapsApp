package com.example.straymaps.ui.screens.report_a_lost_pet

import android.Manifest
import android.content.ActivityNotFoundException
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.straymaps.R
import com.example.straymaps.ui.misc.HandleSinglePermissionDialog
import com.example.straymaps.ui.misc.ImageSelection
import com.example.straymaps.ui.misc.PhotoAlertDialog
import com.example.straymaps.ui.misc.SaveReportButton
import com.example.straymaps.ui.misc.checkIfRequiredFieldsAreEmpty
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


//Composable function for reporting lost pets
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportALostPet(
    onBackClick: () -> Unit,
    restartApp: (String) -> Unit,
    viewModel: LostPetReportScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val openAlertDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val snackbarHostState = remember { SnackbarHostState() }

    val lostPetUpsertReportEvent by viewModel.lostPetReportUpsertEventSnackbarMessage.collectAsState(initial = null)

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val mediaPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)

    val resultBitmap by viewModel.imagePath.collectAsState()

    var finishButtonEnabled by remember { mutableStateOf(false) }

    //Enables the button to save the report if all the required fields are not empty
    finishButtonEnabled = checkIfRequiredFieldsAreEmpty(
        viewModel.lostPetReport.lostPetType,
        viewModel.lostPetReport.lostPetColour,
        viewModel.lostPetReport.lostPetAppearanceDescription,
        viewModel.lostPetReport.lostPetLastKnownLocation
    )

    //Calls on a method from ViewModel to check whether the user is logged in
    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp)
    }

    //Checks if Camera permission is granted, if not then it launches a permission request
    if (!cameraPermissionState.status.isGranted) {
        HandleSinglePermissionDialog(
            permissionState = cameraPermissionState,
            onPermissionRequest = { cameraPermissionState.launchPermissionRequest() }
        )
    }

    //Checks if Media permission is granted, if not then it launches a permission request
    if (!mediaPermissionState.status.isGranted) {
        HandleSinglePermissionDialog(
            permissionState = mediaPermissionState,
            onPermissionRequest = { mediaPermissionState.launchPermissionRequest() }
        )
    }

    /** If the user wants to upload a photo, they can choose between gallery and camera
     *  This part launches gallery or camera, depending on what the user picks
     */

    //Launcher for gallery
    val launcherForPickingImageFromGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {uri ->
        if (uri != null){
            viewModel.getMetaDataThenResizeAndSave(uri, viewModel.sizeOfImageShown.width, viewModel.sizeOfImageShown.height)
        }
        openAlertDialog.value = false
    }

    var cameraLaunchedSuccessfully by remember { mutableStateOf(false) }

    //Launcher for camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) {success ->
        if (success){
            viewModel.capturedImagePath.value?.let { uri ->
                viewModel.getMetaDataThenResizeAndSave(uri, viewModel.sizeOfImageShown.width, viewModel.sizeOfImageShown.height)
            }
            cameraLaunchedSuccessfully = true
        }
        openAlertDialog.value = false
    }

    /** If true, opens a Dialog that asks the user whether they'd like to pick a photo
     *  from Gallery or take one with their camera
     */
    if (openAlertDialog.value) {
        PhotoAlertDialog(
            onDismissRequest = { openAlertDialog.value = false },
            onPickFromGallery = {
                launcherForPickingImageFromGallery.launch("image/*")
            },
            onTakeAPhoto = {
                viewModel.imageProcessing()
            }
        )
    }

    //Provides the initial Uri for the image that the user takes with their camera
    LaunchedEffect(viewModel.capturedImagePath){
        viewModel.capturedImagePath.collect{ capturedPictureUri ->
            capturedPictureUri?.let{
                Log.d("CameraLaunch", "Launching camera with URI: $it")
                try {
                    takePictureLauncher.launch(it)
                } catch (e: ActivityNotFoundException) {
                    Log.e("StartingCamera", "Camera app not found", e)
                }
            }
        }
    }

    /** Launch effect that checks if the user successfully saved a report
     *  and shows a snackbar message accordingly
     */
    LaunchedEffect(lostPetUpsertReportEvent) {
        lostPetUpsertReportEvent?.let { isSuccess ->
            val messageResource = if (isSuccess) R.string.report_saved
                                else R.string.error_saving_report
            val message = context.getString(messageResource)
            snackbarHostState.showSnackbar(message)
        }
    }

    //The UI
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.file_a_lost_pet_report_top_app_bar_text),
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
        ) {
            ImageSelection(
                resultBitmap = resultBitmap,
                openAlertDialog = openAlertDialog) { newSize ->
                viewModel.onSizeChange(newSize)
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            PetDetails(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            SaveReportButton(
                enabled = finishButtonEnabled,
                coroutineScope = coroutineScope,
                onSaveClick = { viewModel.saveLostPetReport() },
                text = stringResource(id = R.string.finish_filing)
            )
        }
    }
}

//Composable function that serves as a pattern for report TextFields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDetailTextField(
    value: String,
    labelResId: Int,
    onValueChange: (String) -> Unit,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = stringResource(id = labelResId),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier = Modifier.fillMaxWidth(),
        isError = isError
    )
}

//LazyColumn that shows all the TextFields of a report, utilizing PetDetailTextField function
@Composable
fun PetDetails(
    viewModel: LostPetReportScreenViewModel,
    modifier: Modifier
){
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        item {
            Text(
                text = stringResource(id = R.string.required_fields_pet),
                style = MaterialTheme.typography.titleSmall
            )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetType,
                labelResId = R.string.type_of_pet,
                onValueChange = viewModel::updateLostPetReportType,
                isError = viewModel.lostPetReport.lostPetType.isEmpty()
                )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetName,
                labelResId = R.string.pet_name,
                onValueChange = viewModel::updateLostPetReportName,
            )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetColour,
                labelResId = R.string.pet_colour,
                onValueChange = viewModel::updateLostPetReportColour,
                isError = viewModel.lostPetReport.lostPetColour.isEmpty()
            )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetSex,
                labelResId = R.string.pet_sex,
                onValueChange = viewModel::updateLostPetReportSex)
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetAppearanceDescription,
                labelResId = R.string.pet_appearance,
                onValueChange = viewModel::updateLostPetReportAppearanceDescription,
                isError = viewModel.lostPetReport.lostPetAppearanceDescription.isEmpty()
            )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetLastKnownLocation,
                labelResId = R.string.pet_location,
                onValueChange = viewModel::updateLostPetReportLocation,
                isError = viewModel.lostPetReport.lostPetLastKnownLocation.isEmpty())
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetMicrochipId,
                labelResId = R.string.pet_microchip,
                onValueChange = viewModel::updateLostPetReportMicrochipId )
        }

        item {
            PetDetailTextField(
                value = viewModel.lostPetReport.lostPetAdditionalInformation,
                labelResId = R.string.animal_additional_information,
                onValueChange = viewModel::updateLostPetReportAdditionalInformation)
        }
    }
}