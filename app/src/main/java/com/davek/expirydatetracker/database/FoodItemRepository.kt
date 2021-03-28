package com.davek.expirydatetracker.database

import androidx.lifecycle.LiveData
import com.davek.expirydatetracker.util.SortType

interface FoodItemRepository {

    fun observeSortedFoodItems(sortType: SortType): LiveData<DataResult<List<FoodItem>>>

    suspend fun insertFoodItem(foodItem: FoodItem): DataResult<Long>

    suspend fun updateFoodItem(foodItem: FoodItem)

    suspend fun deleteFoodItemById(foodItemId: Long)

    suspend fun getFoodItemById(foodItemId: Long): DataResult<FoodItem>
}