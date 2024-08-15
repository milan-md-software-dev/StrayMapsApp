package com.example.straymaps.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.straymaps.data.stray_animal.StrayAnimal
import com.example.straymaps.data.stray_animal.StrayAnimalDao
import com.example.straymaps.misc.ComposeFileProvider
import com.example.straymaps.misc.DefaultImageProvider
import com.example.straymaps.misc.logError
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Offline Stray Animal Repository"

@Singleton
open class OfflineStrayAnimalRepository @Inject constructor(
    private val strayAnimalDao: StrayAnimalDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
): StrayAnimalRepositoryInterface
    {
        private val cloudFirestoreDatabase = Firebase.firestore
        private var storage = Firebase.storage
        private var storageRef = storage.reference


        //Method that loads all the StrayAnimal reports when the screen is navigated to
        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun loadAllStrayAnimalReports(): Flow<List<StrayAnimal>> {
            return withContext(ioDispatcher) {
                getAllReportsFromCloudFirestoreDatabase()
                strayAnimalDao.getAll()
            }
        }

        //Methods that call on StrayAnimalDao functions for getting reports in different ways
        override fun getSpecificStrayAnimalReport(id: Int): StrayAnimal = strayAnimalDao.getSpecificStrayAnimalReport(id)

        override fun getStrayAnimalByMicrochipId(id: String): StrayAnimal? = strayAnimalDao.getStrayAnimalByMicrochipId(id)

        override fun getAllStrayAnimalOfSpecificType(type: String): Flow<List<StrayAnimal>> = strayAnimalDao.getStrayAnimalByType(type)

        override fun loadAllByType(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllByType()

        override fun loadAllByColour(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllByColour()

        override fun loadAllBySex(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllBySex()

        override fun loadAllByDateAndTime(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllByDateAndTime()

        override fun loadAllNotUploadedReports(): List<StrayAnimal> = strayAnimalDao.loadAllNotUploadedReports()

        /** Method for upserting StrayAnimal reports to the local Database
         *  Whenever there is a report that has not been uploaded to the Firebase Cloud,
         *  this will trigger another method that will try to upload all the reports to the
         *  Cloud that have the value of "strayAnimalIsUploaded" as false
         */
        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun upsertStrayAnimal(strayAnimal: StrayAnimal) {
            withContext(ioDispatcher){
                strayAnimalDao.upsertStrayAnimal(strayAnimal)
                if (!strayAnimal.strayAnimalIsUploaded) {
                    val strayAnimalReportFromRoomDB = loadAllNotUploadedReports()
                    strayAnimalReportFromRoomDB.forEach {
                        try {
                            val uploadSuccess = uploadReportToCloudFirestoreDatabase(it)
                            if (uploadSuccess) {
                                strayAnimalDao.upsertStrayAnimal(strayAnimal = it.copy(strayAnimalIsUploaded = true))
                                Log.d(TAG,"Successfully changed strayAnimal RoomDB->Cloud upload state.")
                            } else {
                                Log.w(TAG, "Error trying to change report upload status in RoomDB.")
                            }
                        } catch (e: Exception) {
                            logError("Error trying to upload reports from RoomDB to the Cloud", e.toString())
                        }
                    }
                }
            }
        }

        //Method for deleting a StrayAnimal report from both the local DB and the Firebase Cloud
        override suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal) {
            withContext(ioDispatcher){
                strayAnimalDao.deleteStrayAnimal(strayAnimal)
                cloudFirestoreDatabase.collection("stray_animal_reports")
                    .document(strayAnimal.strayAnimalReportUniqueId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Stray Animal report successfully deleted from the Cloud.")
                    }
                    .addOnFailureListener{
                        e -> logError("Error deleting Stray Animal report from the Cloud.", e.toString())
                    }
            }
        }

        /** Method for downloading all the reports from the Firebase Cloud into the local DB
         *  Since images are kept in another place, this will download the images and reports
         *  in parallel, and then match the proper images to their corresponding report
         *  based on the image and report unique ID
         */
        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun getAllReportsFromCloudFirestoreDatabase() {
            withContext(ioDispatcher) {
                try {
                    val reportsFromCloud = cloudFirestoreDatabase.collection("stray_animal_reports")
                        .get().await()
                    reportsFromCloud.forEach { report ->
                        val strayAnimalReport = report.toObject(StrayAnimal::class.java)
                        val imageUniqueId = strayAnimalReport.strayAnimalReportUniqueId

                        //Download image and update database in parallel
                        val localFile = downloadImage(imageUniqueId)
                        strayAnimalDao.upsertStrayAnimal(
                            strayAnimal = strayAnimalReport.copy(
                                strayAnimalPhotoPath = localFile?.absolutePath,
                                strayAnimalIsUploaded = true
                            )
                        )
                    }

                } catch (e: Exception) {
                    logError("Error getting reports from the Cloud.", e.toString())
                }
            }
        }

        //Used by method getAllReportsFromCloudFirestoreDatabase to download the images
        private suspend fun downloadImage (imageUniqueId: String): File? {
            return withContext(ioDispatcher) {
                try {
                    val pathReference = storageRef.child("stray_animal_images/${imageUniqueId}")
                    val localFile = File.createTempFile(imageUniqueId, "png")

                    pathReference.getFile(localFile)
                    Log.i(TAG, "Image downloaded successfully from the Cloud.")

                    localFile
                } catch (e: Exception) {
                    logError("Error downloading image from the Cloud.", e.toString())
                    null
                }
            }
        }


        //Used by the method upsertStrayAnimal to upload reports to the Firestore
        private suspend fun uploadReportToCloudFirestoreDatabase(strayAnimal: StrayAnimal): Boolean {
            return withContext(ioDispatcher) {
                try {
                    cloudFirestoreDatabase.collection("stray_animal_reports")
                        .document(strayAnimal.strayAnimalReportUniqueId).set(strayAnimal).await()
                    Log.d(TAG, "StrayAnimal report added to Cloud Firebase.")
                    val file = Uri.fromFile(strayAnimal.strayAnimalPhotoPath?.let { File(it) })
                    val uniqueId = strayAnimal.strayAnimalReportUniqueId
                    val imagesRef: StorageReference = storageRef.child("stray_animal_images/${uniqueId}")
                    val uploadTask = imagesRef.putFile(file)
                    uploadTask.addOnSuccessListener {
                        Log.d(TAG, "Stray Animal report image uploaded to Storage successfully.")
                    }.addOnFailureListener {
                        logError("Stray Animal report image failed to upload to Storage.", it.toString())
                    }
                    true
                } catch (e: Exception) {
                    logError("Error adding the StrayAnimal report", e.toString())
                    false
                }
            }
        }


        //Method that processes the captured image
        fun processCapturedImage(): Uri {
            return ComposeFileProvider.getImageUri(context)
        }

        /** This method saves a drawable as a PNG,
         *  It is used to save Drawable "No image available" as a PNG, and then use its path as the
         *  default path for Stray Animal reports where there is no photo provided
         */
        fun saveDrawableAsPNG(): String? {
            Log.i(TAG, "saveDrawableAsPNG")
            return DefaultImageProvider.getDefaultImagePath(context)
        }

        /** Getting the image from Gallery and making modifications to it
         *  This method gets the image's meta data
         *  Then the next method resizes that image if needed,
         *  and  returns a Bitmap
         */
        fun getMetaDataOfTheImage(uri: Uri): Pair<Int, Int> {
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
                Pair(outWidth, outHeight)
            }
        }

        fun resizeImageFromUriReturnBitmap(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
            }
            return context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int{
            val (height: Int, width: Int) = options.run {outHeight to outWidth}
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth){

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth){
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        //Method that saves the image without resizing
        fun savingImageFromUriAsBitmap(uri: Uri): Bitmap? {
            val contentResolver = context.contentResolver
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                try {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } catch (e: Exception){
                    logError( "Could not save image.", e.toString())
                    null
                }
            } else {
                try {
                    contentResolver.openInputStream(uri)?.use {inputSteam ->
                        BitmapFactory.decodeStream(inputSteam)
                    }
                } catch (e: Exception){
                    logError("Could not save image", e.toString())
                    null
                }
            }
        }

        //Method to save the resized Bitmap to a file in internal storage and get the file path
        fun saveBitmapToFileAndReturnPath(bitmap: Bitmap): String {
            val filename = "Stray_maps_report_image_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            return file.absolutePath
        }

        //Method that decodes the file path and returns an ImageBitmap
        fun loadImageBitmapFromPath(filePath: String?): ImageBitmap? {
            if (filePath.isNullOrBlank()) return null
            return try {
                val bitmap = BitmapFactory.decodeFile(filePath)
                bitmap?.asImageBitmap()
            } catch (e: Exception){
                null
            }
        }
}