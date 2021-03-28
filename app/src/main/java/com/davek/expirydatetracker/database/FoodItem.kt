package com.davek.expirydatetracker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "food_item_table")
data class FoodItem(

    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0L,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "expiryDate")
    var expiryDate: Date,

    @ColumnInfo(name = "quantity")
    var quantity: Int = 1,

    @ColumnInfo(name = "isNotificationOn")
    var isNotificationOn: Boolean = true,

    @ColumnInfo(name = "note")
    var note: String? = null
)
