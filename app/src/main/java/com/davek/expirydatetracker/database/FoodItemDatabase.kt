package com.davek.expirydatetracker.database

import androidx.room.*

@Database(entities = [FoodItem::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class FoodItemDatabase : RoomDatabase() {

    abstract fun foodItemDao(): FoodItemDao
}
