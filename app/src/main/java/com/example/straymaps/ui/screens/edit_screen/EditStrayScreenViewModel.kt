package com.example.straymaps.ui.screens.edit_screen

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.straymaps.data.repositories.OfflineStrayAnimalRepository
import com.example.straymaps.data.stray_animal.StrayAnimal
import com.example.straymaps.misc.Resource
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditStrayScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val strayAnimalRepository: OfflineStrayAnimalRepository,
): StrayMapsViewModel() {

    //Initial size of the photo on the report
    var onSizeChanged by mutableStateOf(IntSize.Zero)
        private set

    //Method to change the size of the photo
    fun onSizeChange(newSize: IntSize){
        onSizeChanged = newSize
    }

    private val TAG = "EditStrayScreenViewModel"

    //Variable that holds the value of the strayAnimal report
    var editableStrayAnimalReport by mutableStateOf(StrayAnimal())
        private set

    private val _editStrayReportUpsertEventSnackbarMessage = MutableStateFlow<Boolean?>(null)
    val editStrayReportUpsertEventSnackbarMessage: StateFlow<Boolean?> = _editStrayReportUpsertEventSnackbarMessage.asStateFlow()

    private val _resizedBitmap = MutableStateFlow<Bitmap?>(null)
    private val resizedBitmap: StateFlow<Bitmap?> = _resizedBitmap.asStateFlow()

    private val _imagePath = MutableStateFlow(strayAnimalRepository.loadImageBitmapFromPath(editableStrayAnimalReport.strayAnimalPhotoPath))
    val imagePath: StateFlow<ImageBitmap?> = _imagePath.asStateFlow()

    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()

    /** Method that gets the strayAnimal report id through SavedStateHandle and then calls on
     *  getSpecificReport method to get that report
     */
    init {
        val reportId: Int = checkNotNull(savedStateHandle.get<Int>("reportId"))
        getSpecificReport(reportId)
    }

    private fun getSpecificReport(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val report = strayAnimalRepository.getSpecificStrayAnimalReport(id)
            editableStrayAnimalReport = report
        }
    }

    //Updates the editableStrayAnimalReport
    private fun editStrayAnimalReport(editFunction: (StrayAnimal) -> StrayAnimal) {
        editableStrayAnimalReport = editFunction(editableStrayAnimalReport)
    }

    private fun editStrayAnimalReportPhotoPath(photoPath: String){
        editStrayAnimalReport { it.copy(strayAnimalPhotoPath = photoPath) }
    }

    fun editStrayAnimalReportType(type: String) {
        editStrayAnimalReport { it.copy(strayAnimalType = type) }
    }

    fun editStrayAnimalReportColour(colour: String){
        editStrayAnimalReport { it.copy(strayAnimalColour = colour) }
    }

    fun editStrayAnimalReportSex(sex: String){
        editStrayAnimalReport { it.copy(strayAnimalSex = sex) }
    }

    fun editStrayAnimalReportAppearanceDescription(appearance: String){
        editStrayAnimalReport { it.copy(strayAnimalAppearanceDescription = appearance) }
    }

    fun editStrayAnimalReportLocation(location: String){
        editStrayAnimalReport { it.copy(strayAnimalLocationDescription = location) }
    }

    fun editStrayAnimalReportMicrochipId(microchipId: String){
        editStrayAnimalReport { it.copy(strayAnimalMicrochipID = microchipId) }
    }

    fun editStrayAnimalReportContactInformation(info: String){
        editStrayAnimalReport { it.copy(strayAnimalContactInformation = info) }
    }

    fun editStrayAnimalReportAdditionalInformation(info: String){
        editStrayAnimalReport { it.copy(strayAnimalAdditionalInformation = info) }
    }

    //Variable that holds the complete edited report to be saved in saveEditedStrayAnimalReport method
    private var completeEditedStrayAnimalReport by mutableStateOf(
        StrayAnimal()
    )

    /** Saves the strayAnimal edited report but first changes the value of strayAnimalIsUploaded to
     *  false, as to trigger another uploaded to the Cloud after it's been saved to the local DB
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditedStrayAnimalReport(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                completeEditedStrayAnimalReport = editableStrayAnimalReport.copy(
                    strayAnimalIsUploaded = false
                )
                strayAnimalRepository.upsertStrayAnimal(completeEditedStrayAnimalReport)
                Resource.Success("Report edited successfully.")
            }
            catch (e: Exception) {
                Log.e(TAG, "Error editing stray animal report.", e)
                Resource.Error("Error editing stray animal report.")
            }
            _editStrayReportUpsertEventSnackbarMessage.value = when(result) {
                is Resource.Success -> true
                is Resource.Error -> false
                else -> null
            }
        }
    }


    /** This method takes the image that the user provides and gets its metadata
     *  then resizes it if its out of the bounds that the image that is shown on the report
     *  needs to be,
     *  and finally saves the image and updates the report's file path
     */
    fun getMetaDataThenResizeAndSave(uri: Uri, reqWidth: Int, reqHeight: Int) {
        viewModelScope.launch {
            val (width, height) = withContext(Dispatchers.IO) {
                strayAnimalRepository.getMetaDataOfTheImage(uri)
            }

            if (width > reqWidth || height > reqHeight) {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    strayAnimalRepository.resizeImageFromUriReturnBitmap(uri, reqWidth, reqHeight)
                }
                _resizedBitmap.value = properSizedBitmap }
            else {
                val properSizedBitmap = withContext(Dispatchers.IO){
                    strayAnimalRepository.savingImageFromUriAsBitmap(uri)
                }
                _resizedBitmap.value = properSizedBitmap
            }

            resizedBitmap.value?.let { bitmap ->
                val filePath = withContext(Dispatchers.IO) {
                    strayAnimalRepository.saveBitmapToFileAndReturnPath(bitmap)
                }
                Log.d("Debug", "File path: $filePath")
                _imagePath.value = strayAnimalRepository.loadImageBitmapFromPath(filePath)
                editStrayAnimalReportPhotoPath(filePath)
            }
        }
    }

    //Provides the initial URI for camera image capture
    fun imageProcessing(){
        val uri = strayAnimalRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }

}