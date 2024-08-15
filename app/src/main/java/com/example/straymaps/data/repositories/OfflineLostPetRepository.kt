package com.example.straymaps.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.data.lost_pet.LostPetDao
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

private const val TAG = "Offline Lost Pet Repository"

@Singleton
class OfflineLostPetRepository @Inject constructor(
    private val lostPetDao: LostPetDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
): LostPetRepositoryInterface
    {
        private val cloudFirebaseDatabase = Firebase.firestore
        private val storage = Firebase.storage
        private var storageRef = storage.reference

        //Method that loads all the LostPet reports when the screen is navigated to
        override suspend fun loadAllLostPetReports(): Flow<List<LostPet>> {
            return withContext(ioDispatcher) {
                getAllReportsFromCloudFirestoreDatabase()
                lostPetDao.getAll()
            }
        }

        //Methods that call on LostPetDao functions for getting reports in different ways
        override fun getSpecificLostPetReport(id: Int): LostPet = lostPetDao.getSpecificLostPetReport(id)

        override fun getLostPetsByMicrochipId(id: String): LostPet = lostPetDao.getLostPetByMicrochipId(id)

        override fun getLostPetsByName(name: String): Flow<List<LostPet>> = lostPetDao.getLostPetsByName(name)

        override fun getAllLostPetsOfSpecificType(type: String): Flow<List<LostPet>> = lostPetDao.getLostPetByType(type)

        override fun loadAllByType(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsByType()

        override fun loadAllByColour(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsByColours()

        override fun loadAllBySex(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsBySex()

        override fun loadAllByDateAndTime(): Flow<List<LostPet>> = lostPetDao.loadAllByDateAndTime()

        override fun loadAllNotUploadedReports(): List<LostPet> = lostPetDao.loadAllNotUploadedReports()

        /** Method for upserting LostPet reports to the local Database
         *  Whenever there is a report that has not been uploaded to the Firebase Cloud,
         *  this will trigger another method that will try to upload all the reports to the
         *  Cloud that have the value of "lostPetIsUploaded" as false
         */
        override suspend fun upsertLostPet(lostPet: LostPet) {
            withContext(ioDispatcher){
                lostPetDao.upsertLostPet(lostPet)
                if (!lostPet.lostPetIsUploaded) {
                    val lostPetReportFromRoomDB = loadAllNotUploadedReports()
                    lostPetReportFromRoomDB.forEach {
                        try {
                            val uploadSuccess = uploadReportToCloudFirebaseDatabase(it)
                            if (uploadSuccess) {
                                lostPetDao.upsertLostPet(lostPet = it.copy(lostPetIsUploaded = true))
                                Log.d(TAG, "Successfully changed lostPet RoomDB->Cloud upload state.")
                            } else {
                                Log.w(TAG, "Error trying to change report upload status in RoomDB.")
                            }
                        } catch (e: Exception) {
                            logError("Error trying to upload reports from RoomDB to the Cloud.", e.toString())
                        }
                    }
                }
            }
        }

        //Method for deleting a LostPet report from both the local DB and the Firebase Cloud
        override suspend fun deleteLostPet(lostPet: LostPet) {
            withContext(ioDispatcher){
                lostPetDao.deleteLostPet(lostPet)
                cloudFirebaseDatabase.collection("lost_pet_reports")
                    .document(lostPet.lostPetReportUniqueId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Lost Pet report successfully deleted from the Cloud.")
                    }
                    .addOnFailureListener {
                        e -> logError("Error deleting Lost Pet report from the Cloud.", e.toString())
                    }
            }
        }

        /** Method for downloading all the reports from the Firebase Cloud into the local DB
         *  Since images are kept in another place, this will download the images and reports
         *  in parallel, and then match the proper images to their corresponding report
         *  based on the image and report unique ID
         */
        private suspend fun getAllReportsFromCloudFirestoreDatabase() {
            withContext(ioDispatcher) {
                try {
                    val reportsFromCloud = cloudFirebaseDatabase.collection("lost_pet_reports")
                        .get().await()
                    reportsFromCloud.forEach { report ->
                        val lostPetReport = report.toObject(LostPet::class.java)
                        val imageUniqueId = lostPetReport.lostPetReportUniqueId

                        //Download image and update database in parallel
                        val localFile = downloadImage(imageUniqueId)
                        lostPetDao.upsertLostPet(
                            lostPet = lostPetReport.copy(
                                lostPetPhoto = localFile?.absolutePath,
                                lostPetIsUploaded = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting reports from the Cloud.", e)
                }
            }
        }

        //Used by method getAllReportsFromCloudFirestoreDatabase to download the images
        private suspend fun downloadImage(imageUniqueId: String): File? {
            return withContext(ioDispatcher) {
                try {
                    val pathReference = storageRef.child("lost_pet_images/${imageUniqueId}")
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

        //Used by the method upsertLostPet to upload reports to the Firestore
        private suspend fun uploadReportToCloudFirebaseDatabase(lostPet: LostPet): Boolean {
            return withContext(ioDispatcher) {
                try {
                    cloudFirebaseDatabase.collection("lost_pet_reports")
                        .document(lostPet.lostPetReportUniqueId).set(lostPet).await()
                    Log.d(TAG, "LostPet report added to Cloud Firebase")
                    val file = Uri.fromFile(lostPet.lostPetPhoto?.let { File(it) })
                    val uniqueId = lostPet.lostPetIsUploaded
                    val imagesRef: StorageReference = storageRef.child("lost_pet_images/${uniqueId}")
                    val uploadTask = imagesRef.putFile(file)
                    uploadTask.addOnSuccessListener {
                        Log.d(TAG, "Lost Pet report image uploaded to Storage successfully.")
                    }
                        .addOnFailureListener{
                            Log.e(TAG, "Lost Pet report image failed to upload to Storage.", it)
                        }
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding the LostPet report.", e)
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
         *  default path for Lost Pet reports where there is no photo provided
         */
        fun saveDrawableAsPNG(): String? {
            return DefaultImageProvider.getDefaultImagePath(context)
        }

        /** Getting the image from Gallery and making modifications to it
         *  This method gets the image's meta data
         *  Then the next method resizes that image if needed,
         *  and  returns a Bitmap
         */
        fun getMetaDataOfTheImage(uri: Uri): Pair<Int, Int> {
            Log.d("getMetaDataOfTheGalleryImage", "URI: $uri")
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

        //Method function saves the image without resizing
        fun savingImageFromUriAsBitmap(uri: Uri): Bitmap? {
            val contentResolver = context.contentResolver
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                try {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } catch (e: Exception){
                    Log.e("SavingImageFromGallery", "Could not save image.", e)
                    null
                }
            } else {
                try {
                    contentResolver.openInputStream(uri)?.use {inputSteam ->
                        BitmapFactory.decodeStream(inputSteam)
                    }
                } catch (e: Exception){
                    Log.e("SavingImageFromGallery", "Could not save image", e)
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