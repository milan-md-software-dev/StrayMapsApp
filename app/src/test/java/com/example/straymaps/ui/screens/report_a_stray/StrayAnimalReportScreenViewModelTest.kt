package com.example.straymaps.ui.screens.report_a_stray

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.data.repositories.OfflineStrayAnimalRepository
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class StrayAnimalReportScreenViewModelTest {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var strayAnimalRepository: OfflineStrayAnimalRepository

    @Mock
    private lateinit var accountServiceInterface: AccountServiceInterface

    private lateinit var viewModel: StrayAnimalReportScreenViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val filePath = "test/path"


    @Before
    fun setup(){
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = StrayAnimalReportScreenViewModel(
            strayAnimalRepository,
           accountServiceInterface
        )
    }

    @Test
    fun getMetaDataThenResizeAndSave_success_successfullyResizesAndSavesBitmap()= runTest {
        val uri = Mockito.mock(Uri::class.java)
        val reqWidth = 100
        val reqHeight = 100

        val width = 200
        val height = 200
        val bitmap = Mockito.mock(Bitmap::class.java)


        Mockito.`when`(strayAnimalRepository.getMetaDataOfTheImage(uri)).thenReturn(Pair(width,height))

        Mockito.`when`(strayAnimalRepository.resizeImageFromUriReturnBitmap(uri,reqWidth, reqHeight)).thenReturn(bitmap)

        Mockito.`when`(strayAnimalRepository.saveBitmapToFileAndReturnPath(bitmap)).thenReturn(filePath)

        viewModel.getMetaDataThenResizeAndSave(uri,reqWidth, reqHeight)

        advanceUntilIdle()

        val result = viewModel.resizedBitmap.first()

        Truth.assertThat(bitmap.equals(result))

    }

}