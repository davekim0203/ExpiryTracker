package com.davek.expirydatetracker.database

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.davek.expirydatetracker.MainCoroutineRule
import com.davek.expirydatetracker.getOrAwaitValue
import com.davek.expirydatetracker.util.getCalendarInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class FoodItemDaoTest {

    private lateinit var database: FoodItemDatabase
    private lateinit var itemDao: FoodItemDao

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            FoodItemDatabase::class.java
        ).allowMainThreadQueries().build()
        itemDao = database.foodItemDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun verifyInsertFoodItemAndGetById() = runBlockingTest {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 3,
            isNotificationOn = false,
            note = "Some Note"
        )
        itemDao.insert(foodItem)
        val loaded = itemDao.getFoodItemById(foodItem.itemId)

        assertThat(loaded as FoodItem, notNullValue())
        assertEquals(foodItem.itemId, loaded.itemId)
        assertEquals(foodItem.name, loaded.name)
        assertEquals(foodItem.expiryDate, loaded.expiryDate)
        assertEquals(foodItem.quantity, loaded.quantity)
        assertEquals(foodItem.isNotificationOn, loaded.isNotificationOn)
    }

    @Test
    fun verifyUpdate() = runBlockingTest {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 1,
            isNotificationOn = true,
            note = "Some Note"
        )
        itemDao.insert(foodItem)

        foodItem.apply {
            name = "Updated name"
            expiryDate = getCalendarInstance().apply { set(1999, 2, 22) }.time
            quantity = 2
            isNotificationOn = false
            note = null
        }

        itemDao.update(foodItem)
        val loaded = itemDao.getFoodItemById(foodItem.itemId)

        assertThat(loaded as FoodItem, notNullValue())
        assertEquals(foodItem.itemId, loaded.itemId)
        assertEquals(foodItem.name, loaded.name)
        assertEquals(foodItem.expiryDate, loaded.expiryDate)
        assertEquals(foodItem.quantity, loaded.quantity)
        assertEquals(foodItem.isNotificationOn, loaded.isNotificationOn)
        assertEquals(foodItem.note, loaded.note)
    }

    @Test
    fun verifyObserveFoodItemsSortedByExpiryDateAsc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByExpiryDateAsc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(106L, allFoodItems[0].itemId)
        assertEquals(105L, allFoodItems[1].itemId)
        assertEquals(104L, allFoodItems[2].itemId)
        assertEquals(103L, allFoodItems[3].itemId)
        assertEquals(102L, allFoodItems[4].itemId)
        assertEquals(101L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyObserveFoodItemsSortedByExpiryDateDesc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByExpiryDateDesc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(101L, allFoodItems[0].itemId)
        assertEquals(102L, allFoodItems[1].itemId)
        assertEquals(103L, allFoodItems[2].itemId)
        assertEquals(104L, allFoodItems[3].itemId)
        assertEquals(105L, allFoodItems[4].itemId)
        assertEquals(106L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyObserveFoodItemsSortedByQuantityAsc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByQuantityAsc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(106L, allFoodItems[0].itemId)
        assertEquals(105L, allFoodItems[1].itemId)
        assertEquals(104L, allFoodItems[2].itemId)
        assertEquals(103L, allFoodItems[3].itemId)
        assertEquals(102L, allFoodItems[4].itemId)
        assertEquals(101L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyObserveFoodItemsSortedByQuantityDesc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByQuantityDesc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(101L, allFoodItems[0].itemId)
        assertEquals(102L, allFoodItems[1].itemId)
        assertEquals(103L, allFoodItems[2].itemId)
        assertEquals(104L, allFoodItems[3].itemId)
        assertEquals(105L, allFoodItems[4].itemId)
        assertEquals(106L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyObserveFoodItemsSortedByNameAsc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByNameAsc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(101L, allFoodItems[0].itemId)
        assertEquals(106L, allFoodItems[1].itemId)
        assertEquals(105L, allFoodItems[2].itemId)
        assertEquals(103L, allFoodItems[3].itemId)
        assertEquals(104L, allFoodItems[4].itemId)
        assertEquals(102L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyObserveFoodItemsSortedByNameDesc() = runBlockingTest {
        insertTestFoodItems()
        val allFoodItems = itemDao.observeFoodItemsSortedByNameDesc().getOrAwaitValue()

        assertThat(allFoodItems, notNullValue())
        assertEquals(102L, allFoodItems[0].itemId)
        assertEquals(104L, allFoodItems[1].itemId)
        assertEquals(103L, allFoodItems[2].itemId)
        assertEquals(105L, allFoodItems[3].itemId)
        assertEquals(106L, allFoodItems[4].itemId)
        assertEquals(101L, allFoodItems[5].itemId)
    }

    @Test
    fun verifyDeleteItemById() = runBlockingTest {
        insertTestFoodItems()
        val itemsBeforeDelete = itemDao.observeFoodItemsSortedByNameDesc().getOrAwaitValue()
        val beforeDelete = itemsBeforeDelete.firstOrNull { it.itemId == 101L }
        assertThat(beforeDelete, notNullValue())

        itemDao.deleteItemById(101L)
        val itemsAfterDelete = itemDao.observeFoodItemsSortedByNameDesc().getOrAwaitValue()
        val afterDelete = itemsAfterDelete.firstOrNull { it.itemId == 101L }
        assertThat(afterDelete, nullValue())
    }

    private suspend fun insertTestFoodItems() {
        insertTestFoodItem(101L, "Bacon", 2020, 6, 18, 6)
        insertTestFoodItem(102L, "Milk", 2020, 6, 8, 5)
        insertTestFoodItem(103L, "Ham", 2020, 5, 8, 4)
        insertTestFoodItem(104L, "Juice", 2019, 10, 8, 3)
        insertTestFoodItem(105L, "Cookies", 2019, 10, 7, 2)
        insertTestFoodItem(106L, "Butter", 2018, 6, 8, 1)
    }

    private suspend fun insertTestFoodItem(
        id: Long,
        name: String,
        year: Int, month: Int, date: Int,
        quantity: Int
    ) {
        val expiryDate = getCalendarInstance().apply { set(year, month, date) }
        val foodItem = FoodItem(id, name, expiryDate.time, quantity)
        itemDao.insert(foodItem)
    }
}