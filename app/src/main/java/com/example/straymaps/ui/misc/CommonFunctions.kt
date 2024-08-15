package com.example.straymaps.ui.misc

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import com.example.straymaps.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


//Composable function that handles single permissions such as camera or media
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleSinglePermissionDialog(
    permissionState: PermissionState,
    onPermissionRequest: () -> Unit
) {
    val permissionText = if (permissionState.status.shouldShowRationale){
        stringResource(id = R.string.Please_grant_camera_permission)
    } else {
        stringResource(id = R.string.Camera_required_for_this_feature)
    }

    PermissionDialog(
        permissionState = permissionState,
        rationaleText = permissionText,
        onPermissionRequest = { onPermissionRequest() },
        actionText = stringResource(id = R.string.request_permissions)
    )
}

//Composable function that appears in case a permission needs to be granted
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(
    permissionState: PermissionState,
    rationaleText: String,
    onPermissionRequest: () -> Unit,
    actionText: String
) {
    if (permissionState.status.isGranted) {
        Text(stringResource(id = R.string.permission_granted))
    } else {
        Column{
            Text(text = rationaleText)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Button(onClick = onPermissionRequest) {
                Text(text = actionText)
            }
        }
    }
}

//AlertDialog that allows the user to pick the source of the photograph for the report
@Composable
fun PhotoAlertDialog(
    onDismissRequest: () -> Unit,
    onTakeAPhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                Icons.Filled.Photo,
                contentDescription = stringResource(id = R.string.photo_icon)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.photo_alert_dialog_title)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.photo_alert_dialog_body)
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onTakeAPhoto()
                }
            ) {
                Text(stringResource(id = R.string.photo_alert_dialog_camera))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onPickFromGallery()
                }
            ) {
                Text(stringResource(id = R.string.photo_alert_dialog_gallery))
            }
        }
    )
}

//Composable function that implements a button for saving a report
@Composable
fun SaveReportButton(
    enabled: Boolean,
    coroutineScope: CoroutineScope,
    onSaveClick: suspend () -> Unit,
    text: String
){
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            enabled = enabled,
            onClick = {
                coroutineScope.launch {
                    onSaveClick()
                }
            }
        ) {
            Text(
                text = text)
        }
    }
}


//Composable function that shows the image selected
@Composable
fun ImageSelection(
    resultBitmap: ImageBitmap?,
    openAlertDialog: MutableState<Boolean>,
    onSizeChanged: (IntSize) -> Unit,
) {
    Row (
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(2f)
        ) {
            resultBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = stringResource(id = R.string.report_photo_placeholder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openAlertDialog.value = true }
                        .onSizeChanged { newSize -> onSizeChanged(newSize) }
                )
            }
            Text(
                text = stringResource(id = R.string.please_upload_a_pet_photo),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}

//Function that checks whether the required fields are filled out appropriately
fun checkIfRequiredFieldsAreEmpty(type: String, color: String, description: String, location: String): Boolean{
    return type.isNotEmpty() && color.isNotEmpty() && description.isNotEmpty() && location.isNotEmpty()
}