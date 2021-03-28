package com.davek.expirydatetracker.di

import android.content.Context
import androidx.room.Room
import com.davek.expirydatetracker.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideFoodItemDatabase(@ApplicationContext context: Context): FoodItemDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FoodItemDatabase::class.java,
            "food_item_table"
        ).build()
    }

    @Singleton
    @Provides
    fun provideFoodItemLocalDataSource(
        foodItemDatabase: FoodItemDatabase,
        ioDispatcher: CoroutineDispatcher
    ): FoodItemDataSource {
        return FoodItemLocalDataSource(
            foodItemDatabase.foodItemDao(), ioDispatcher
        )
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

/**
 * The binding for FoodItemRepository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object FoodItemRepositoryModule {

    @Singleton
    @Provides
    fun provideFoodItemRepository(
        localFoodItemDataSource: FoodItemDataSource,
        ioDispatcher: CoroutineDispatcher
    ): FoodItemRepository {
        return DefaultFoodItemRepository(localFoodItemDataSource, ioDispatcher)
    }
}