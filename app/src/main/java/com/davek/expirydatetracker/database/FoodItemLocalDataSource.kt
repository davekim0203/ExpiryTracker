package com.davek.expirydatetracker.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davek.expirydatetracker.util.SortType
import kotlinx.coroutines.*
import java.lang.Exception

class FoodItemLocalDataSource constructor(
    private val foodItemDao: FoodItemDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FoodItemDataSource {

    override fun observeSortedFoodItems(sortType: SortType): LiveData<DataResult<List<FoodItem>>> {
        return Transformations.map(
            when (sortType) {
                SortType.REMAINING_DAYS_ASC -> foodItemDao.observeFoodItemsSortedByExpiryDateAsc()
                SortType.REMAINING_DAYS_DESC -> foodItemDao.observeFoodItemsSortedByExpiryDateDesc()
                SortType.QUANTITY_ASC -> foodItemDao.observeFoodItemsSortedByQuantityAsc()
                SortType.QUANTITY_DESC -> foodItemDao.observeFoodItemsSortedByQuantityDesc()
                SortType.NAME_ASC -> foodItemDao.observeFoodItemsSortedByNameAsc()
                SortType.NAME_DESC -> foodItemDao.observeFoodItemsSortedByNameDesc()
            }
        ) {
            DataResult.Success(it)
        }
    }

    override suspend fun insertFoodItem(foodItem: FoodItem): DataResult<Long> =
        withContext(ioDispatcher) {
            try {
                val foodItemId = foodItemDao.insert(foodItem)
                return@withContext DataResult.Success(foodItemId)
            } catch (e: Exception) {
                return@withContext DataResult.Error(e)
            }
        }

    override suspend fun updateFoodItem(foodItem: FoodItem) = withContext(ioDispatcher) {
        foodItemDao.update(foodItem)
    }

    override suspend fun deleteFoodItemById(foodItemId: Long) = withContext(ioDispatcher) {
        foodItemDao.deleteItemById(foodItemId)
    }

    override suspend fun getFoodItem(foodItemId: Long): DataResult<FoodItem> =
        withContext(ioDispatcher) {
            try {
                val foodItem = foodItemDao.getFoodItemById(foodItemId)
                if (foodItem != null) {
                    return@withContext DataResult.Success(foodItem)
                } else {
                    return@withContext DataResult.Error(Exception("FoodItem is not found"))
                }
            } catch (e: Exception) {
                return@withContext DataResult.Error(e)
            }
        }
}