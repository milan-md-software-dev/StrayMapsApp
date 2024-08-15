package com.example.straymaps.di

import android.content.Context
import androidx.room.Room
import com.example.straymaps.data.StrayMapsDatabase
import com.example.straymaps.data.firebase.AccountService
import com.example.straymaps.data.firebase.AccountServiceInterface
import com.example.straymaps.misc.Constants.STRAY_MAPS_DATABASE
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

//Dependency Injection object
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideStrayMapsDatabase(
        @ApplicationContext context: Context
    ) =
        Room.databaseBuilder(
            context,
            StrayMapsDatabase::class.java,
            STRAY_MAPS_DATABASE
        ).fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()


    @Provides
    @Singleton
    fun provideStrayAnimalDao(db: StrayMapsDatabase) = db.strayAnimalDao()

    @Provides
    @Singleton
    fun provideLostPetsDao(db: StrayMapsDatabase) = db.lostPetDAo()

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
}


@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun provideAccountService(impl: AccountService): AccountServiceInterface
}