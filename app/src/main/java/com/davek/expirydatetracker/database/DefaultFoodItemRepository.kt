package com.davek.expirydatetracker.database

import androidx.lifecycle.LiveData
import com.davek.expirydatetracker.util.SortType
import kotlinx.coroutines.*

class DefaultFoodItemRepository constructor(
    private val foodItemLocalDataSource: FoodItemDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FoodItemRepository {

    override fun observeSortedFoodItems(sortType: SortType): LiveData<DataResult<List<FoodItem>>> {
        return foodItemLocalDataSource.observeSortedFoodItems(sortType)
    }

    override suspend fun insertFoodItem(foodItem: FoodItem): DataResult<Long> {
        return foodItemLocalDataSource.insertFoodItem(foodItem)
    }

    override suspend fun updateFoodItem(foodItem: FoodItem) {
        withContext(ioDispatcher) {
            launch {
                foodItemLocalDataSource.updateFoodItem(foodItem)
            }
        }
    }

    override suspend fun deleteFoodItemById(foodItemId: Long) {
        withContext(ioDispatcher) {
            launch {
                foodItemLocalDataSource.deleteFoodItemById(foodItemId)
            }
        }
    }

    override suspend fun getFoodItemById(foodItemId: Long): DataResult<FoodItem> {
        return foodItemLocalDataSource.getFoodItem(foodItemId)
    }
}