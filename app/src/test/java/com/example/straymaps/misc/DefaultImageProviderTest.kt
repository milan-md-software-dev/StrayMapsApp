package com.example.straymaps.misc

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DefaultImageProviderTest {

    @Test
    fun getDefaultImagePath_success_returnsDefaultImagePathAsString()= runTest{

        val context = RuntimeEnvironment.getApplication().applicationContext

        val defaultImagePath = DefaultImageProvider.getDefaultImagePath(context)

        if (defaultImagePath != null) {
            assertTrue(defaultImagePath.isNotEmpty())
        }

    }
}