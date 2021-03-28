package com.davek.expirydatetracker.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FoodItemDao {

    @Insert
    suspend fun insert(item: FoodItem): Long

    @Update
    suspend fun update(item: FoodItem)

    @Query("SELECT * FROM food_item_table WHERE itemId = :foodItemId")
    suspend fun getFoodItemById(foodItemId: Long): FoodItem?

    @Query("SELECT * FROM food_item_table ORDER BY expiryDate ASC")
    fun observeFoodItemsSortedByExpiryDateAsc(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_item_table ORDER BY expiryDate DESC")
    fun observeFoodItemsSortedByExpiryDateDesc(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_item_table ORDER BY quantity ASC")
    fun observeFoodItemsSortedByQuantityAsc(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_item_table ORDER BY quantity DESC")
    fun observeFoodItemsSortedByQuantityDesc(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_item_table ORDER BY name ASC")
    fun observeFoodItemsSortedByNameAsc(): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_item_table ORDER BY name DESC")
    fun observeFoodItemsSortedByNameDesc(): LiveData<List<FoodItem>>

    @Query("DELETE FROM food_item_table WHERE itemId = :foodItemId")
    suspend fun deleteItemById(foodItemId: Long)
}