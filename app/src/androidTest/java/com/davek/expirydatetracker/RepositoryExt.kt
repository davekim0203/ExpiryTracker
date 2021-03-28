package com.davek.expirydatetracker

import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import kotlinx.coroutines.runBlocking

fun FoodItemRepository.insertItemBlocking(item: FoodItem) = runBlocking {
    this@insertItemBlocking.insertFoodItem(item)
}