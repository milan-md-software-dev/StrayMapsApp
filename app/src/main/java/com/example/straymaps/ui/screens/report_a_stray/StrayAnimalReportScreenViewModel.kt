package com.example.straymaps.ui.screens.report_a_stray

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
import com.example.straymaps.data.repositories.OfflineStrayAnimalRepository
import com.example.straymaps.data.stray_animal.StrayAnimal
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
class StrayAnimalReportScreenViewModel @Inject constructor(
    private val strayAnimalRepository: OfflineStrayAnimalRepository,
    private val accountService: AccountServiceInterface,
): StrayMapsViewModel(){

    //Initial size of the photo on the report
    var sizeOfImageShown by mutableStateOf(IntSize.Zero)
        private set

    //Method to change the size of the photo
    fun onSizeChange (newSize: IntSize){
        sizeOfImageShown = newSize
    }

    private val TAG = "StrayAnimalReportScreenViewModel"

    /** This part initializes the default value for when there is no image available
     *  It calls a function from the repository, which calls on "DefaultImageProvider" object
     *  located in ImageUtils.kt
     */

    private var defaultNoImageAvailablePath: String? = null

    init {
        defaultNoImageAvailablePath = initializeDefaultNoImagePath()
    }

    private val defaultNoImageAvailableBitmap = strayAnimalRepository.loadImageBitmapFromPath(defaultNoImageAvailablePath)

    private val _strayReportUpsertEventSnackbarMessage = MutableStateFlow<Boolean?>(null)
    val strayReportUpsertEventSnackbarMessage: StateFlow<Boolean?> = _strayReportUpsertEventSnackbarMessage.asStateFlow()

    private val _imagePath = MutableStateFlow(defaultNoImageAvailableBitmap)
    val imagePath: StateFlow<ImageBitmap?> = _imagePath.asStateFlow()

    private val _resizedBitmap = MutableStateFlow<Bitmap?>(null)
    val resizedBitmap: StateFlow<Bitmap?> = _resizedBitmap.asStateFlow()

    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()


    private fun initializeDefaultNoImagePath(): String? {
        return strayAnimalRepository.saveDrawableAsPNG()
    }

    /** This method checks if the user is logged in, if not then it redirects
     *  them to the splash screen
     */
    fun initialize(restartApp:(String) -> Unit){
        launchCatching {
            accountService.currentUser.collect{ user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    /** This object represents a "default state" of a Stray Animal Report,
    *   Before the user makes modifications
    *   Below are functions that the Screen calls to modify each field individually
     *  It is used for UI purposes
    */
    var strayAnimalReport by mutableStateOf(
        StrayAnimal(
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
        null,
        false,
        "",
        ""
        )
    )
        private set


    private fun updateStrayAnimalReport (updateFunction: (StrayAnimal) -> StrayAnimal) {
        strayAnimalReport = updateFunction(strayAnimalReport)
    }

    private fun updateStrayAnimalReportPhotoPath(photoPath: String){
        updateStrayAnimalReport { it.copy(strayAnimalPhotoPath = photoPath) }
    }

    fun updateStrayAnimalReportType(type: String) {
        updateStrayAnimalReport { it.copy(strayAnimalType = type) }
    }

    fun updateStrayAnimalReportColour(colour: String) {
        updateStrayAnimalReport { it.copy(strayAnimalColour = colour) }
    }

    fun updateStrayAnimalReportSex(sex: String) {
        updateStrayAnimalReport { it.copy(strayAnimalSex = sex) }
    }

    fun updateStrayAnimalReportAppearanceDescription(appearance: String) {
        updateStrayAnimalReport { it.copy(strayAnimalAppearanceDescription = appearance) }
    }

    fun updateStrayAnimalReportLocation(location: String) {
        updateStrayAnimalReport { it.copy(strayAnimalLocationDescription = location) }
    }

    fun updateStrayAnimalReportMicrochipId(microchipId: String) {
        updateStrayAnimalReport { it.copy(strayAnimalMicrochipID = microchipId) }
    }

    fun updateStrayAnimalReportContactInformation(info: String) {
        updateStrayAnimalReport { it.copy(strayAnimalContactInformation = info) }
    }

    fun updateStrayAnimalReportAdditionalInformation(info: String){
        updateStrayAnimalReport { it.copy(strayAnimalAdditionalInformation = info) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateStrayAnimalReportDateAndTime(value: LocalDateTime){
        val localDateTimeToString = Converters().fromLocalDateTime(value)
        strayAnimalReport = strayAnimalReport.copy(strayAnimalReportDateAndTime = localDateTimeToString)
    }

    private fun addStrayAnimalReportUniqueId(id: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalReportUniqueId = id)
    }

    /** This object is what what "strayAnimalReport" from above
    *   will be copied onto, and then with some subsequent modifications
     *  - such as adding date and time, be saved and uploaded
    */
    private var completeStrayAnimalReport by mutableStateOf(
        StrayAnimal(
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
    suspend fun saveStrayAnimalReport() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                //Adding the local date and time when the report is being made
                val now  = LocalDateTime.now()
                updateStrayAnimalReportDateAndTime(now)
                //Adding the UniqueID used to identify the report
                val uniqueId = UUID.randomUUID().toString()
                addStrayAnimalReportUniqueId(uniqueId)
                //Creating the user's UID (obtained from Firebase Auth)
                val userUniqueId = accountService.currentUserId

                //Converting the microchipID to uppercase, and adding user's unique ID
                completeStrayAnimalReport = strayAnimalReport.copy(
                    strayAnimalMicrochipID = strayAnimalReport.strayAnimalMicrochipID?.uppercase(Locale.getDefault(),
                    ),
                    strayAnimalReportMadeByUserId = userUniqueId
                )

                //Upserts the report
                strayAnimalRepository.upsertStrayAnimal(completeStrayAnimalReport)
                resetStrayAnimalReportValuesToDefault()
                Resource.Success("Report filed successfully.")
            }
                catch (e: Exception) {
                Log.e(TAG, "Error saving stray animal report", e)
                Resource.Error("Error saving Stray Animal report")
                }
                _strayReportUpsertEventSnackbarMessage.value = when (result) {
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

    fun getMetaDataThenResizeAndSave(uri: Uri, reqWidth: Int, reqHeight: Int){
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
                    updateStrayAnimalReportPhotoPath(filePath)
                }
        }
    }


    //Method to reset the fields of the report in UI after the user saves their report
    private fun resetStrayAnimalReportValuesToDefault(){
        strayAnimalReport = StrayAnimal(
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
            null,
            false,
            "",
            ""
        )
    }

    //Provides the initial URI for camera image capture
    fun imageProcessing(){
        val uri = strayAnimalRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }
}



