package com.example.straymaps.ui.screens.report_a_lost_pet

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.data.repositories.OfflineLostPetRepository
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
class LostPetReportScreenViewModelTest {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var lostPetRepository: OfflineLostPetRepository

    @Mock
    private lateinit var accountServiceInterface: AccountServiceInterface

    private lateinit var viewModel: LostPetReportScreenViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val filePath = "test/path"

    @Before
    fun setup(){
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = LostPetReportScreenViewModel(
            lostPetRepository,
            accountServiceInterface
        )
    }

    @Test
    fun getMetaDataThenResizeAndSave()= runTest{
        val uri = Mockito.mock(Uri::class.java)
        val reqWidth = 100
        val reqHeight = 100

        val width = 200
        val height = 200
        val bitmap = Mockito.mock(Bitmap::class.java)

        Mockito.`when`(lostPetRepository.getMetaDataOfTheImage(uri)).thenReturn(Pair(width,height))

        Mockito.`when`(lostPetRepository.resizeImageFromUriReturnBitmap(uri, reqWidth, reqHeight)).thenReturn(bitmap)

        Mockito.`when`(lostPetRepository.saveBitmapToFileAndReturnPath(bitmap)).thenReturn(filePath)

        viewModel.getMetaDataThenResizeAndSave(uri, reqWidth, reqHeight)

        advanceUntilIdle()

        val result = viewModel.resizedBitmap.first()

        Truth.assertThat(bitmap.equals(result))


    }
}