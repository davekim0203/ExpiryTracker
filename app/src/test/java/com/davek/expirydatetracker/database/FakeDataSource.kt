package com.davek.expirydatetracker.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.davek.expirydatetracker.util.SortType
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class FakeDataSource(var foodItems: MutableList<FoodItem> = mutableListOf()) : FoodItemDataSource {

    override suspend fun insertFoodItem(foodItem: FoodItem): DataResult<Long> {
        foodItems.add(foodItem)
        return DataResult.Success(foodItem.itemId)
    }

    override suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItems.firstOrNull { it.itemId == foodItem.itemId }?.let {
            it.name = foodItem.name
            it.expiryDate = foodItem.expiryDate
            it.quantity = foodItem.quantity
            it.isNotificationOn = foodItem.isNotificationOn
            it.note = foodItem.note
        }
    }

    override suspend fun deleteFoodItemById(foodItemId: Long) {
        foodItems.removeIf { it.itemId == foodItemId }
    }

    override suspend fun getFoodItem(foodItemId: Long): DataResult<FoodItem> {
        foodItems.firstOrNull { it.itemId == foodItemId }?.let { return DataResult.Success(it) }
        return DataResult.Error(
            Exception("Food item not found")
        )
    }

    override fun observeSortedFoodItems(sortType: SortType): LiveData<DataResult<List<FoodItem>>> {
        val sortedItemsLiveData = MutableLiveData<DataResult<List<FoodItem>>>()
        val sortedItems = when (sortType) {
            SortType.REMAINING_DAYS_ASC -> foodItems.sortedBy { it.expiryDate }
            SortType.REMAINING_DAYS_DESC -> foodItems.sortedByDescending { it.expiryDate }
            SortType.QUANTITY_ASC -> foodItems.sortedBy { it.quantity }
            SortType.QUANTITY_DESC -> foodItems.sortedByDescending { it.quantity }
            SortType.NAME_ASC -> foodItems.sortedBy { it.name }
            SortType.NAME_DESC -> foodItems.sortedByDescending { it.name }
        }
        runBlocking {
            sortedItemsLiveData.value = DataResult.Success(sortedItems)
        }

        return sortedItemsLiveData
    }
}