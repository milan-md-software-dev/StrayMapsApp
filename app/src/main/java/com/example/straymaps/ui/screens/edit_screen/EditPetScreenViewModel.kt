package com.example.straymaps.ui.screens.edit_screen

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.data.repositories.OfflineLostPetRepository
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
class EditPetScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lostPetRepository: OfflineLostPetRepository
): StrayMapsViewModel() {

    //Initial size of the photo on the report
    var onSizeChanged by mutableStateOf(IntSize.Zero)
        private set

    //Method to change the size of the photo
    fun onSizeChange(newSize: IntSize){
        onSizeChanged = newSize
    }

    private val TAG = "EditLostPetScreenViewModel"

    //Variable that holds the value of the lostPet report
    var editableLostPetReport by mutableStateOf(LostPet())
        private set

    private val _editLstPetReportUpsertEventSnackbarMessage = MutableStateFlow<Boolean?>(null)
    val editLostPetReportUpsertEventSnackbarMessage: StateFlow<Boolean?> = _editLstPetReportUpsertEventSnackbarMessage.asStateFlow()

    private val _resizedBitmap = MutableStateFlow<Bitmap?>(null)
    private val resizedBitmap: StateFlow<Bitmap?> = _resizedBitmap.asStateFlow()

    private val _imagePath = MutableStateFlow(lostPetRepository.loadImageBitmapFromPath(editableLostPetReport.lostPetPhoto))
    val imagePath: StateFlow<ImageBitmap?> = _imagePath.asStateFlow()

    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()

    /** Method that gets the lostPet report id through SavedStateHandle and then calls on
     *  getSpecificReport method to get that report
     */
    init{
        val reportId: Int = checkNotNull(savedStateHandle.get<Int>("reportId"))
        getSpecificReport(reportId)
    }

    private fun getSpecificReport(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val report = lostPetRepository.getSpecificLostPetReport(id)
            editableLostPetReport = report
        }
    }

    //Updates the editableLostPetReport
    private fun editLostPetReport(editFunction: (LostPet) -> LostPet) {
        editableLostPetReport = editFunction(editableLostPetReport)
    }

    private fun editLostPetReportPhotoPath(photoPath: String) {
        editLostPetReport { it.copy(lostPetPhoto = photoPath) }
    }

    fun editLostPetReportType(type: String) {
        editLostPetReport { it.copy(lostPetType = type) }
    }

    fun editLostPetReportName(name: String){
        editLostPetReport { it.copy(lostPetName = name) }
    }

    fun editLostPetReportColour(colour: String) {
        editLostPetReport { it.copy(lostPetColour = colour) }
    }

    fun editLostPetReportSex(sex: String){
        editLostPetReport { it.copy(lostPetSex = sex) }
    }

    fun editLostPetReportAppearanceDescription(appearance: String){
        editLostPetReport { it.copy(lostPetAppearanceDescription = appearance) }
    }

    fun editLostPetReportLocation(location: String){
        editLostPetReport { it.copy(lostPetLastKnownLocation = location) }
    }

    fun editLostPetReportMicrochipId(microchipId: String){
        editLostPetReport { it.copy(lostPetMicrochipId = microchipId) }
    }

    fun editLostPetReportContactInformation(info: String) {
        editLostPetReport { it.copy(lostPetContactInformation = info) }
    }

    fun editLostPetReportAdditionalInformation(info: String){
        editLostPetReport { it.copy(lostPetAdditionalInformation = info) }
    }

    //Variable that holds the complete edited report to be saved in saveEditedLostPetReport method
    private var completeEditedLostPetReport by mutableStateOf(
        LostPet()
    )

    /** Saves the lostPet edited report but first changes the value of lostPetIsUploaded to
     *  false, as to trigger another uploaded to the Cloud after it's been saved to the local DB
     */
    suspend fun saveEditedLostPetReport(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                completeEditedLostPetReport = editableLostPetReport.copy(
                    lostPetIsUploaded = false
                )
                lostPetRepository.upsertLostPet(completeEditedLostPetReport)
                Resource.Success("Report edited successfully.")
            }
            catch (e: Exception) {
                Log.e(TAG, "Error editing lost pet report.", e)
                Resource.Error("Error editing lost pet report.")
            }
            _editLstPetReportUpsertEventSnackbarMessage.value = when(result) {
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
                lostPetRepository.getMetaDataOfTheImage(uri)
            }

            if (width > reqWidth || height > reqHeight) {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    lostPetRepository.resizeImageFromUriReturnBitmap(uri, reqWidth, reqHeight)
                }
                _resizedBitmap.value = properSizedBitmap }
            else {
                val properSizedBitmap = withContext(Dispatchers.IO){
                    lostPetRepository.savingImageFromUriAsBitmap(uri)
                }
                _resizedBitmap.value = properSizedBitmap
            }

            resizedBitmap.value?.let { bitmap ->
                val filePath = withContext(Dispatchers.IO) {
                    lostPetRepository.saveBitmapToFileAndReturnPath(bitmap)
                }
                Log.d("Debug", "File path: $filePath")
                _imagePath.value = lostPetRepository.loadImageBitmapFromPath(filePath)
                editLostPetReportPhotoPath(filePath)
            }
        }
    }

    //Provides the initial URI for camera image capture
    fun imageProcessing(){
        val uri = lostPetRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }

}