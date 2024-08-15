package com.example.straymaps.misc

import android.net.Uri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ComposeFileProviderTest{

    @Test
    fun testGetImageUri_success_returnsUri()= runTest {

        val context = RuntimeEnvironment.getApplication().applicationContext

        val uri: Uri = ComposeFileProvider.getImageUri(context)

        assertTrue(uri.toString().startsWith("content://${context.packageName}.fileprovider/"))

        val imageFile = File(context.cacheDir, "images")

        assertTrue(imageFile.exists())
        assertTrue(imageFile.isDirectory)
        assertTrue(imageFile.listFiles()?.isNotEmpty()==true)
    }
}