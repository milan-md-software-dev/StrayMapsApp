package com.example.straymaps.data.repositories

import com.example.straymaps.data.stray_animal.StrayAnimal
import kotlinx.coroutines.flow.Flow

interface StrayAnimalRepositoryInterface {

    //Retrieves all the items from the given data source
    suspend fun loadAllStrayAnimalReports(): Flow<List<StrayAnimal>>

    //Retrieves one specific report, by primary key
    fun getSpecificStrayAnimalReport(id: Int): StrayAnimal

    //Retrieves all the items from the given data source that match with the [microchip id]
    fun getStrayAnimalByMicrochipId(id: String): StrayAnimal?

    //Retrieves all the items from the given data source that match with the [type]
    fun getAllStrayAnimalOfSpecificType(type: String): Flow<List<StrayAnimal>>

    //Retrieves all the items from the given data source sorted by [type]
    fun loadAllByType(): Flow<List<StrayAnimal>>

    //Retrieves all the items from the given data source sorted by [colour]
    fun loadAllByColour(): Flow<List<StrayAnimal>>

    //Retrieves all the items from the given data source sorted by [sex]
    fun loadAllBySex(): Flow<List<StrayAnimal>>

    //Retrieves all the items from the given data source sorted by [date and time]
    fun loadAllByDateAndTime(): Flow<List<StrayAnimal>>

    fun loadAllNotUploadedReports(): List<StrayAnimal>

    //Upserts item in the data source
    suspend fun upsertStrayAnimal(strayAnimal: StrayAnimal)

    //Deletes item from the data source
    suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal)

}