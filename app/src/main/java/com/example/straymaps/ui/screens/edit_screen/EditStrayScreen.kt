package com.example.straymaps.ui.screens.edit_screen

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
import androidx.compose.material3.SnackbarHost
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

//Composable function for editing stray animal reports that have already been filed
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditStrayScreen(
    onBackClick:() -> Unit,
    viewModel: EditStrayScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val openAlertDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val snackbarHostState = remember { SnackbarHostState() }

    val strayReportEditUpsertEvent by viewModel.editStrayReportUpsertEventSnackbarMessage.collectAsState(
        null
    )

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val mediaPermissionState =
        rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)

    val resultBitmap by viewModel.imagePath.collectAsState()

    var cameraLaunchedSuccessfully by remember { mutableStateOf(false) }

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
    ) { uri ->
        if (uri != null) {
            viewModel.getMetaDataThenResizeAndSave(
                uri,
                viewModel.onSizeChanged.width,
                viewModel.onSizeChanged.height
            )
        }
        openAlertDialog.value = false
    }


    //Launcher for camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.capturedImagePath.value?.let { uri ->
                viewModel.getMetaDataThenResizeAndSave(
                    uri,
                    viewModel.onSizeChanged.width,
                    viewModel.onSizeChanged.height
                )
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
    LaunchedEffect(viewModel.capturedImagePath) {
        viewModel.capturedImagePath.collect { capturedPictureUri ->
            capturedPictureUri?.let {
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
     */    LaunchedEffect(strayReportEditUpsertEvent) {
        strayReportEditUpsertEvent?.let { isSuccess ->
            val messageResource =   if (isSuccess) R.string.report_edited_successfully
                                    else R.string.report_editing_failed
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
                        text = stringResource(id = R.string.edit_report),
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
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
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

            StrayDetailsForm(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            SaveReportButton(
                enabled = true,
                coroutineScope = coroutineScope,
                onSaveClick = {viewModel.saveEditedStrayAnimalReport()},
                text = stringResource(id = R.string.finish_editing)
            )
        }
    }
}

//Composable function that serves as a pattern for report TextFields
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayDetailTextField(
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

//LazyColumn that shows all the TextFields of a report, utilizing StrayDetailTextField function
@Composable
fun StrayDetailsForm(
    viewModel: EditStrayScreenViewModel,
    modifier: Modifier
) {
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        item {
            Text(
                text = stringResource(id = R.string.required_fields_stray),
                style = MaterialTheme.typography.titleSmall
            )
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalType,
                labelResId = R.string.type_of_animal,
                onValueChange = viewModel::editStrayAnimalReportType,
                isError = viewModel.editableStrayAnimalReport.strayAnimalType.isEmpty()
                )}

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalColour,
                labelResId = R.string.animal_colour,
                onValueChange = viewModel::editStrayAnimalReportColour,
                isError = viewModel.editableStrayAnimalReport.strayAnimalColour.isEmpty()
            )
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalSex,
                labelResId = R.string.animal_sex,
                onValueChange = viewModel::editStrayAnimalReportSex)
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalAppearanceDescription,
                labelResId = R.string.animal_appearance,
                onValueChange = viewModel::editStrayAnimalReportAppearanceDescription,
                isError = viewModel.editableStrayAnimalReport.strayAnimalAppearanceDescription.isEmpty()
                )
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalLocationDescription,
                labelResId = R.string.animal_location,
                onValueChange = viewModel::editStrayAnimalReportLocation,
                isError = viewModel.editableStrayAnimalReport.strayAnimalLocationDescription.isEmpty()
            )
        }

        item{
            viewModel.editableStrayAnimalReport.strayAnimalMicrochipID?.let {
                StrayDetailTextField(
                    value = it,
                    labelResId = R.string.animal_microchip,
                    onValueChange = viewModel::editStrayAnimalReportMicrochipId)
            }
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalContactInformation,
                labelResId = R.string.animal_contact_information,
                onValueChange = viewModel::editStrayAnimalReportContactInformation)
        }

        item{
            StrayDetailTextField(
                value = viewModel.editableStrayAnimalReport.strayAnimalAdditionalInformation,
                labelResId = R.string.animal_additional_information,
                onValueChange = viewModel::editStrayAnimalReportAdditionalInformation)
        }



    }
}