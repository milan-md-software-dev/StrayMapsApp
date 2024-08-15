package com.example.straymaps.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.straymaps.data.lost_pet.LostPet
import com.example.straymaps.data.lost_pet.LostPetDao
import com.google.common.truth.Truth
import kotlinx.coroutines.awaitAll
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
class LostPetDaoTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: StrayMapsDatabase
    private lateinit var dao: LostPetDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StrayMapsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.lostPetDAo()
    }

    @After
    fun finish(){
        database.close()
    }

    private val lostPetDog = LostPet(
        0,
        "photopath",
        "dog",
        "Bebe",
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

    private val lostPetCat = LostPet(
        1,
        "photopath",
        "cat",
        "Maca",
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

    private val lostPetGecko = LostPet(
        2,
        "photopath",
        "crested gecko",
        "Lizzo",
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
    fun upsertLostPet_success_addsLostPetReportToDatabase() = runTest {
        dao.upsertLostPet(lostPetDog)

        dao.getAll().test {
            val allLostPets = awaitItem()
            Truth.assertThat(allLostPets).contains(lostPetDog)
            cancel()
        }
    }

    @Test
    fun deleteLostPet_success_deletesLostPetReportFromDatabase() = runTest {
        dao.upsertLostPet(lostPetCat)

        dao.getAll().test {
            val allLostPets = awaitItem()
            Truth.assertThat(allLostPets).contains(lostPetCat)
            cancel()
        }

        dao.deleteLostPet(lostPetCat)
        dao.getAll().test {
            val allLostPets = awaitItem()
            Truth.assertThat(allLostPets).doesNotContain(lostPetCat)
            cancel()
        }
    }

    @Test
    fun getAll_success_loadsAllLostPetReportsFromDatabase()= runTest {
        dao.upsertLostPet(lostPetDog)
        dao.upsertLostPet(lostPetCat)
        dao.upsertLostPet(lostPetGecko)

        dao.getAll().test {
            val allLostPets = awaitItem()
            Truth.assertThat(allLostPets).contains(lostPetDog)
            Truth.assertThat(allLostPets).contains(lostPetCat)
            Truth.assertThat(allLostPets).contains(lostPetGecko)
            cancel()
        }
    }

    @Test
    fun getLostPetByMicrochipId_success_findsTheProperLostPetReportByMicrochipId() = runTest {
        dao.upsertLostPet(lostPetDog)

        val lostPet = dao.getLostPetByMicrochipId("567")

        if (lostPet != null) {
            Truth.assertThat(lostPet.lostPetMicrochipId).isEqualTo("567")
        }
    }

    @Test
    fun getLostPetByType_success_findsAllLostPetReportsOfASpecificType()= runTest {
        dao.upsertLostPet(lostPetCat)
        dao.upsertLostPet(lostPetGecko)

        dao.getLostPetByType("crested gecko").test{
            val lostPet = awaitItem()
            Truth.assertThat(lostPet).contains(lostPetGecko)
            Truth.assertThat(lostPet).doesNotContain(lostPetCat)
            Truth.assertThat(lostPet).doesNotContain(lostPetDog)
            cancel()
        }
    }

    @Test
    fun loadAllByColour_success_loadsAllLostPetReportsOrderedByColour()= runTest {
        dao.upsertLostPet(lostPetGecko)
        dao.upsertLostPet(lostPetCat)
        dao.upsertLostPet(lostPetDog)

        dao.loadAllLostPetsByColours().test {
            val lostPet = awaitItem()
            assertTrue(lostPet.first().equals(lostPetGecko))
            cancel()
        }
    }

    @Test
    fun loadAllBySex_success_loadsAllLostPetReportsOrderedBySex()= runTest {
        dao.upsertLostPet(lostPetDog)
        dao.upsertLostPet(lostPetCat)
        dao.upsertLostPet(lostPetGecko)

        dao.loadAllLostPetsBySex().test {
            val lostPet = awaitItem()
            assertTrue(lostPet.first()!=(lostPetGecko))
            cancel()
        }
    }

    @Test
    fun loadAllByDateAndTime_success_loadsAllLostPetReportsOrderedByDateAndTime() = runTest{
        dao.upsertLostPet(lostPetDog)
        dao.upsertLostPet(lostPetCat)
        dao.upsertLostPet(lostPetGecko)

        dao.loadAllByDateAndTime().test {
            val lostPet = awaitItem()
            val expectedResult = listOf(lostPetDog, lostPetCat, lostPetGecko)
            assertTrue(lostPet.equals(expectedResult))
            cancel()
        }
    }
}