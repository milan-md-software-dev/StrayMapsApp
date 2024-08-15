package com.example.straymaps.data



import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.straymaps.data.stray_animal.StrayAnimal
import com.example.straymaps.data.stray_animal.StrayAnimalDao
import com.google.common.truth.Truth
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime


@RunWith(AndroidJUnit4::class)
@SmallTest
class StrayAnimalDaoTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: StrayMapsDatabase
    private lateinit var dao: StrayAnimalDao

    @Before
    fun setup (){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrayMapsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.strayAnimalDao()
    }

    @After
    fun finish (){
        database.close()
    }

    private val strayAnimalDog = StrayAnimal(
        0,
        "photopath",
        "dog",
        "white",
        "female",
        "small white terrier dog",
        "Vracar, Belgrade",
        "123",
        "Miles, +123456789",
        "very cute",
        LocalDateTime.of(2022,1,2,12,12,12,12).toString(),
        false,
        "123-123-123",
        "miles"
    )

    private val strayAnimalCat = StrayAnimal(
        1,
        "photopath",
        "cat",
        "black and white",
        "female",
        "siamese cat",
        "Novi Beograd",
        "456",
        "Nick, +123456789",
        "likes napping",
        LocalDateTime.of(2023,1,2,12,12,12,12).toString(),
        false,
        "234-234-234",
        "mariah"
    )

    private val strayAnimalGecko = StrayAnimal(
        2,
        "photopath",
        "crested gecko",
        "beige",
        "male",
        "adult crested gecko",
        "Zvezdara, Belgrade",
        "789",
        "Miles, +123456789",
        "has one single brain cell",
        LocalDateTime.of(2024,1,2,12,12,12,12).toString(),
        false,
        "345-345-345",
        "fernando"
    )

    @Test
    fun upsertStrayAnimal_success_addsStrayAnimalReportToDatabase() = runTest  {
        dao.upsertStrayAnimal(strayAnimalDog)

        dao.getAll().test {
            val allStrayAnimals = awaitItem()
            Truth.assertThat(allStrayAnimals).contains(strayAnimalDog)
            cancel()
        }
    }

    @Test
    fun deleteStrayAnimal_success_deletesStrayAnimalReportFromDatabase() = runTest {
        dao.upsertStrayAnimal(strayAnimalCat)

        dao.getAll().test {
            val allStrayAnimals = awaitItem()
            Truth.assertThat(allStrayAnimals).contains(strayAnimalCat)
            cancel()
        }

        dao.deleteStrayAnimal(strayAnimalCat)
        dao.getAll().test {
            val allStrayAnimals = awaitItem()
            Truth.assertThat(allStrayAnimals).doesNotContain(strayAnimalCat)
            cancel()
        }
    }

    @Test
    fun getAll_success_loadsAllStrayAnimalReportsFromDatabase() = runTest() {
        dao.upsertStrayAnimal(strayAnimalDog)
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.getAll().test {
            val allStrayAnimals = awaitItem()
            Truth.assertThat(allStrayAnimals).contains(strayAnimalDog)
            Truth.assertThat(allStrayAnimals).contains(strayAnimalCat)
            Truth.assertThat(allStrayAnimals).contains(strayAnimalGecko)
            cancel()
        }

    }

    @Test
    fun getStrayAnimalByMicrochipId_success_findsTheProperStrayAnimalReportByMicrochipId() = runTest() {
        dao.upsertStrayAnimal(strayAnimalDog)

        val strayAnimal = dao.getStrayAnimalByMicrochipId("123")

        if (strayAnimal != null) {
            Truth.assertThat(strayAnimal.strayAnimalMicrochipID).isEqualTo("123")
        }
    }

    @Test
    fun getStrayAnimalByType_success_findsAllStrayAnimalReportsOfASpecificType() = runTest() {
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.getStrayAnimalByType("cat").test {
            val strayAnimal = awaitItem()
            Truth.assertThat(strayAnimal).contains(strayAnimalCat)
            Truth.assertThat(strayAnimal).doesNotContain(strayAnimalDog)
            Truth.assertThat(strayAnimal).doesNotContain(strayAnimalGecko)
            cancel()
        }
    }

    @Test
    fun loadAllByType_success_loadsAllStrayAnimalReportsOrderedByType() = runTest() {
        dao.upsertStrayAnimal(strayAnimalDog)
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.loadAllByType().test {
            val strayAnimal = awaitItem()
            assertTrue(strayAnimal.first().equals(strayAnimalCat))
            assertTrue(strayAnimal.first()!=(strayAnimalGecko))
            cancel()
        }
    }

    @Test
    fun loadAllByColour_success_loadsAllStrayAnimalReportsOrderedByColour() = runTest() {
        dao.upsertStrayAnimal(strayAnimalDog)
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.loadAllByColour().test {
            val strayAnimal = awaitItem()
            assertTrue(strayAnimal.first().equals(strayAnimalGecko))
            cancel()
        }
    }

    @Test
    fun loadAllBySex_success_loadsAllStrayAnimalReportsOrderedBySex() = runTest() {
        dao.upsertStrayAnimal(strayAnimalDog)
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.loadAllBySex().test {
            val strayAnimal = awaitItem()
            assertTrue(strayAnimal.first()!=(strayAnimalGecko))
            cancel()
        }
    }

    @Test
    fun loadAllByDateAndTime_success_loadsAllStrayAnimalReportsOrderedByDateAndTime() = runTest {
        dao.upsertStrayAnimal(strayAnimalDog)
        dao.upsertStrayAnimal(strayAnimalCat)
        dao.upsertStrayAnimal(strayAnimalGecko)

        dao.loadAllByDateAndTime().test {
            val strayAnimal = awaitItem()
            val expectedResult = listOf(strayAnimalDog, strayAnimalCat, strayAnimalGecko)
            assertTrue(strayAnimal.equals(expectedResult))
            cancel()
        }
    }

}

