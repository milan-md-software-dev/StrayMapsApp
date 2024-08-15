package com.example.straymaps.ui.screens.report_a_lost_pet

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
import androidx.lifecycle.viewModelScope
import com.example.straymaps.data.Converters
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.data.repositories.OfflineLostPetRepository
import com.example.straymaps.misc.Resource
import com.example.straymaps.misc.StrayMapsScreen
import com.example.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class LostPetReportScreenViewModel @Inject constructor(
    private val lostPetRepository: OfflineLostPetRepository,
    private val accountService: AccountServiceInterface
) :StrayMapsViewModel(){

    //Initial size of the photo on the report
    var sizeOfImageShown by mutableStateOf(IntSize.Zero)
        private set

    //Method to change the size of the photo
    fun onSizeChange(newSize: IntSize){
        sizeOfImageShown = newSize
    }

    private val TAG = "LostPetReportScreenViewModel"

    /** This part initializes the default value for when there is no image available
     *  It calls a function from the repository, which calls on "DefaultImageProvider" object
     *  located in ImageUtils.kt
     */

    private var defaultNoImageAvailablePath: String? = null

    init {
        defaultNoImageAvailablePath = initializeDefaultNoImagePath()
    }

    private val defaultNoImageAvailableBitmap = lostPetRepository.loadImageBitmapFromPath(defaultNoImageAvailablePath)

    private val _lostPetReportUpsertEventSnackbarMessage = MutableStateFlow<Boolean?>(null)
    val lostPetReportUpsertEventSnackbarMessage: StateFlow<Boolean?> = _lostPetReportUpsertEventSnackbarMessage.asStateFlow()

    private val _imagePath = MutableStateFlow(defaultNoImageAvailableBitmap)
    val imagePath: StateFlow<ImageBitmap?> = _imagePath.asStateFlow()

    private val _resizedBitmap = MutableStateFlow<Bitmap?>(null)
    val resizedBitmap: StateFlow<Bitmap?> = _resizedBitmap.asStateFlow()

    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()


    private fun initializeDefaultNoImagePath(): String? {
        return lostPetRepository.saveDrawableAsPNG()
    }

    /** This method checks if the user is logged in, if not then it redirects
     *  them to the splash screen
     */
    fun initialize(restartApp: (String) -> Unit){
        launchCatching {
            accountService.currentUser.collect{user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    /** This object represents a "default state" of a Lost Pet Report,
     *   Before the user makes modifications
     *   Below are functions that the Screen calls to modify each field individually
     *  It is used for UI purposes
     */
    var lostPetReport by mutableStateOf(
        LostPet(
            null,
            defaultNoImageAvailablePath,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null,
            false,
            "",
            ""
        )
    )
        private set

    private fun updateLostPetReport(updateFunction: (LostPet) -> LostPet){
        lostPetReport = updateFunction(lostPetReport)
    }

    private fun updateLostPetReportPhotoPath(photoPath: String){
        updateLostPetReport { it.copy(lostPetPhoto = photoPath) }
    }

    fun updateLostPetReportName(name: String){
        updateLostPetReport { it.copy(lostPetName = name) }
    }

    fun updateLostPetReportType(type: String){
        updateLostPetReport { it.copy(lostPetType = type) }
    }

    fun updateLostPetReportColour(colour: String){
        updateLostPetReport { it.copy(lostPetColour = colour) }
    }

    fun updateLostPetReportAppearanceDescription(appearance: String){
        updateLostPetReport { it.copy(lostPetAppearanceDescription = appearance) }
    }

    fun updateLostPetReportSex(sex: String){
        updateLostPetReport { it.copy(lostPetSex = sex) }
    }

    fun updateLostPetReportLocation(location: String){
        updateLostPetReport { it.copy(lostPetLastKnownLocation = location) }
    }

    fun updateLostPetReportMicrochipId(microchipId: String){
        updateLostPetReport { it.copy(lostPetMicrochipId = microchipId) }
    }

    fun updateLostPetReportContactInformation(info: String){
        updateLostPetReport { it.copy(lostPetContactInformation = info) }
    }

    fun updateLostPetReportAdditionalInformation(info: String){
        updateLostPetReport { it.copy(lostPetAdditionalInformation = info) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLostPetReportDateAndTime(value: LocalDateTime){
        val localDateTimeToString = Converters().fromLocalDateTime(value)
        lostPetReport = lostPetReport.copy(lostPetReportDateAndTime = localDateTimeToString)
    }

    private fun addLostPetReportUniqueId(id: String) {
        lostPetReport = lostPetReport.copy(lostPetReportUniqueId = id)
    }

    /** This object is what what "lostPetReport" from above
     *   will be copied onto, and then with some subsequent modifications
     *  - such as adding date and time, be saved and uploaded
     */
    private var completeLostPetReport by mutableStateOf(
        LostPet(
            null,
            "none",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null,
            false,
            "",
            ""
        )
    )

    /** This method saves the final report, but first it adds LocalDateTime,
     * as well as a unique ID that the report will have, and a unique ID that
     * the Firebase authentication will give to the user
     */

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveLostPetReport(){
        viewModelScope.launch {
            val result = try {

                //Adding the local date and time when the repost is being created
                val now = LocalDateTime.now()
                updateLostPetReportDateAndTime(now)
                //Adding the UniqueId used to identify the report
                val uniqueId = UUID.randomUUID().toString()
                addLostPetReportUniqueId(uniqueId)
                //Adding the user's UID(obtained from Firebase Auth)
                val userUniqueId = accountService.currentUserId

                completeLostPetReport = lostPetReport.copy(
                    lostPetMicrochipId = lostPetReport.lostPetMicrochipId.uppercase(Locale.getDefault(),
                    ),
                    lostPetReportMadeByUserId = userUniqueId
                )
                lostPetRepository.upsertLostPet(completeLostPetReport)
                resetLostPetReportValuesToDefault()
                Resource.Success("Report filed successfully.")
            }
                catch (e: Exception){
                    Log.e(TAG, "Error saving lost pet report", e)
                    Resource.Error("Error saving Lost Pet report.")
                }
            _lostPetReportUpsertEventSnackbarMessage.value = when (result) {
                is Resource.Success -> true
                is Resource.Error ->  false
                else -> null
            }
        }
    }

    /** This method takes the image that the user provides and gets its metadata
     *  then resizes it if its out of the bounds that the image that is shown on the report
     *  needs to be,
     *  and finally saves the image and updates the report's file path
     */
    fun getMetaDataThenResizeAndSave(uri: Uri, reqWidth: Int, reqHeight: Int){
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
                updateLostPetReportPhotoPath(filePath)
            }
        }
    }

    //Method to reset the fields of the report in UI after the user saves their report
    private fun resetLostPetReportValuesToDefault(){
        lostPetReport = LostPet(
            null,
            "none",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null,
            false,
            "",
            ""
        )
    }

    //Provides the initial URI for camera image capture
    fun imageProcessing(){
        val uri = lostPetRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }



}