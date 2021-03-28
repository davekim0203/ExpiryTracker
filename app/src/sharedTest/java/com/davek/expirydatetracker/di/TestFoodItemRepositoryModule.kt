package com.davek.expirydatetracker.di

import com.davek.expirydatetracker.database.FakeRepository
import com.davek.expirydatetracker.database.FoodItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FoodItemRepository binding to use in tests.
 *
 * Hilt will inject a [FakeRepository] instead of a [DefaultFoodItemRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TestFoodItemRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo: FakeRepository): FoodItemRepository
}
