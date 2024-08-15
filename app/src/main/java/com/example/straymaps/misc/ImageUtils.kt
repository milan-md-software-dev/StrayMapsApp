package com.example.straymaps.misc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.OpenForTesting
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.straymaps.R
import java.io.File
import java.io.FileOutputStream

class ComposeFileProvider: FileProvider(
    R.xml.path_provider
){
    companion object {
        fun getImageUri(context: Context): Uri {

            val directory = File(context.cacheDir, "images")
            directory.mkdirs()

            val file = File.createTempFile(
                "selected_image_",
                null,
                directory
            )

            val authority = context.packageName + ".fileprovider"

            return getUriForFile(
                context,
                authority,
                file
            )
        }
    }
}


/** Provides the default "No image available" PNG file that is used by several different functions.
 *  It first checks whether the defaultImagePath is null or empty, in which case it calls on
 *  createDefaultImage method to create a new Bitmap from a Drawable and return its file path
 */
object DefaultImageProvider {

    private const val TAG = "DefaultImageProvider"

    var defaultImagePath: String? = null

    fun getDefaultImagePath(context: Context): String? {
        //Log.i(TAG, "getDefaultImagePath + ${defaultImagePath.toString()}")
        if (defaultImagePath.isNullOrEmpty()) {
            defaultImagePath = createDefaultImage(context)
        }
        return defaultImagePath
    }

    private fun createDefaultImage(context: Context): String {
            val filename = "no_image_available.png"
            val file = File(context.filesDir,filename)
            return if (file.exists()) {
                //Log.i(TAG, "createDefaultImage + ${defaultImagePath.toString()}")
                file.absolutePath
                } else {
                        try {
                            val drawable = ContextCompat.getDrawable(context, R.drawable.noimageavailable)
                            val bitmap = (drawable as BitmapDrawable).bitmap
                            FileOutputStream(file).use {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 50, it)
                            }
                            file.absolutePath
                        } catch (e: Exception) {
                            //Log.e("saveDrawableAaPNG", e.toString())
                            "Unknown error has occurred."
                        }
        }
    }
}

//Used in NearbyVetClinicsScreen to convert a Drawable resource into a Bitmap file using the function below it
fun bitmapFromDrawableRes(
    context: Context,
    @DrawableRes resourceId: Int
) : Bitmap? {
    return convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))
}


fun convertDrawableToBitmap(
    sourceDrawable: Drawable?
) : Bitmap? {
    if (sourceDrawable == null)
        return null
    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
        val constantState = sourceDrawable.constantState  ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap : Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }

}

