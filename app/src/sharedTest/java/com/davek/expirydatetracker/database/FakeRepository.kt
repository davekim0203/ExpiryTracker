package com.davek.expirydatetracker.database

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.davek.expirydatetracker.util.SortType
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap
import javax.inject.Inject

class FakeRepository @Inject constructor() : FoodItemRepository {

    var foodItems: LinkedHashMap<Long, FoodItem> = LinkedHashMap()

    private val observableFoodItems = MutableLiveData<DataResult<List<FoodItem>>>()

    override fun observeSortedFoodItems(sortType: SortType): LiveData<DataResult<List<FoodItem>>> {
        val unsortedItems = foodItems.values.toList()
        val sortedItems = when (sortType) {
            SortType.REMAINING_DAYS_ASC -> unsortedItems.sortedBy { it.expiryDate }
            SortType.REMAINING_DAYS_DESC -> unsortedItems.sortedByDescending { it.expiryDate }
            SortType.QUANTITY_ASC -> unsortedItems.sortedBy { it.quantity }
            SortType.QUANTITY_DESC -> unsortedItems.sortedByDescending { it.quantity }
            SortType.NAME_ASC -> unsortedItems.sortedBy { it.name }
            SortType.NAME_DESC -> unsortedItems.sortedByDescending { it.name }
        }
        runBlocking { observableFoodItems.value = DataResult.Success(sortedItems) }

        return observableFoodItems
    }

    override suspend fun insertFoodItem(foodItem: FoodItem): DataResult<Long> {
        foodItems[foodItem.itemId] = foodItem
        return DataResult.Success(foodItem.itemId)
    }

    override suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItems[foodItem.itemId] = foodItem
        refreshObservableFoodItems()
    }

    override suspend fun deleteFoodItemById(foodItemId: Long) {
        foodItems.remove(foodItemId)
        refreshObservableFoodItems()
    }

    override suspend fun getFoodItemById(foodItemId: Long): DataResult<FoodItem> {
        foodItems[foodItemId]?.let {
            return DataResult.Success(it)
        }
        return DataResult.Error(Exception("Could not find food item"))
    }

    @VisibleForTesting
    fun addFoodItems(vararg items: FoodItem) {
        for (item in items) {
            foodItems[item.itemId] = item
        }
        runBlocking { refreshObservableFoodItems() }
    }

    private fun refreshObservableFoodItems() {
        observableFoodItems.value = DataResult.Success(foodItems.values.toList())
    }
}