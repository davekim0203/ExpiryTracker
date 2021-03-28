package com.davek.expirydatetracker.database

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davek.expirydatetracker.MainCoroutineRule
import com.davek.expirydatetracker.getOrAwaitValue
import com.davek.expirydatetracker.util.SortType
import com.davek.expirydatetracker.util.getCalendarInstance
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class FoodItemLocalDataSourceTest {

    private lateinit var localDataSource: FoodItemLocalDataSource
    private lateinit var database: FoodItemDatabase

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FoodItemDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = FoodItemLocalDataSource(database.foodItemDao(), Dispatchers.Main)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun verifyInsertFoodItemAndGetById() = mainCoroutineRule.runBlockingTest {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().apply { set(2010, 1, 12) }.time,
            quantity = 3,
            isNotificationOn = false,
            note = "Some Note"
        )
        localDataSource.insertFoodItem(foodItem)
        val loaded = localDataSource.getFoodItem(foodItem.itemId)

        assertThat(loaded is DataResult.Success).isTrue()
        val loadedData = (loaded as DataResult.Success).data

        assertThat(loadedData.itemId).isEqualTo(foodItem.itemId)
        assertThat(loadedData.name).isEqualTo(foodItem.name)
        assertThat(loadedData.expiryDate).isEqualTo(foodItem.expiryDate)
        assertThat(loadedData.quantity).isEqualTo(foodItem.quantity)
        assertThat(loadedData.isNotificationOn).isEqualTo(foodItem.isNotificationOn)
        assertThat(loadedData.note).isEqualTo(foodItem.note)
    }

    @Test
    fun verifyUpdate() = mainCoroutineRule.runBlockingTest {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 1,
            isNotificationOn = true,
            note = "Some Note"
        )
        localDataSource.insertFoodItem(foodItem)

        foodItem.apply {
            name = "Updated name"
            expiryDate = getCalendarInstance().apply { set(1999, 2, 22) }.time
            quantity = 2
            isNotificationOn = false
            note = null
        }

        localDataSource.updateFoodItem(foodItem)
        val loaded = localDataSource.getFoodItem(foodItem.itemId)

        assertThat(loaded is DataResult.Success).isTrue()
        val loadedData = (loaded as DataResult.Success).data

        assertThat(loadedData.itemId).isEqualTo(foodItem.itemId)
        assertThat(loadedData.name).isEqualTo(foodItem.name)
        assertThat(loadedData.expiryDate).isEqualTo(foodItem.expiryDate)
        assertThat(loadedData.quantity).isEqualTo(foodItem.quantity)
        assertThat(loadedData.isNotificationOn).isEqualTo(foodItem.isNotificationOn)
        assertThat(loadedData.note).isEqualTo(foodItem.note)
    }

    @Test
    fun verifyObserveFoodItemsSortedByRemainingDaysAsc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(106L)
        assertThat(loadedData[1].itemId).isEqualTo(105L)
        assertThat(loadedData[2].itemId).isEqualTo(104L)
        assertThat(loadedData[3].itemId).isEqualTo(103L)
        assertThat(loadedData[4].itemId).isEqualTo(102L)
        assertThat(loadedData[5].itemId).isEqualTo(101L)
    }

    @Test
    fun verifyObserveFoodItemsSortedByRemainingDaysDesc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.REMAINING_DAYS_DESC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(101L)
        assertThat(loadedData[1].itemId).isEqualTo(102L)
        assertThat(loadedData[2].itemId).isEqualTo(103L)
        assertThat(loadedData[3].itemId).isEqualTo(104L)
        assertThat(loadedData[4].itemId).isEqualTo(105L)
        assertThat(loadedData[5].itemId).isEqualTo(106L)
    }

    @Test
    fun verifyObserveFoodItemsSortedByQuantityAsc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.QUANTITY_ASC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(106L)
        assertThat(loadedData[1].itemId).isEqualTo(105L)
        assertThat(loadedData[2].itemId).isEqualTo(104L)
        assertThat(loadedData[3].itemId).isEqualTo(103L)
        assertThat(loadedData[4].itemId).isEqualTo(102L)
        assertThat(loadedData[5].itemId).isEqualTo(101L)
    }

    @Test
    fun verifyObserveFoodItemsSortedByQuantityDesc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.QUANTITY_DESC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(101L)
        assertThat(loadedData[1].itemId).isEqualTo(102L)
        assertThat(loadedData[2].itemId).isEqualTo(103L)
        assertThat(loadedData[3].itemId).isEqualTo(104L)
        assertThat(loadedData[4].itemId).isEqualTo(105L)
        assertThat(loadedData[5].itemId).isEqualTo(106L)
    }

    @Test
    fun verifyObserveFoodItemsSortedByNameAsc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.NAME_ASC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(101L)
        assertThat(loadedData[1].itemId).isEqualTo(106L)
        assertThat(loadedData[2].itemId).isEqualTo(105L)
        assertThat(loadedData[3].itemId).isEqualTo(103L)
        assertThat(loadedData[4].itemId).isEqualTo(104L)
        assertThat(loadedData[5].itemId).isEqualTo(102L)
    }

    @Test
    fun verifyObserveFoodItemsSortedByNameDesc() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val allFoodItems =
            localDataSource.observeSortedFoodItems(SortType.NAME_DESC).getOrAwaitValue()

        assertThat(allFoodItems is DataResult.Success).isTrue()
        val loadedData = (allFoodItems as DataResult.Success).data

        assertThat(loadedData[0].itemId).isEqualTo(102L)
        assertThat(loadedData[1].itemId).isEqualTo(104L)
        assertThat(loadedData[2].itemId).isEqualTo(103L)
        assertThat(loadedData[3].itemId).isEqualTo(105L)
        assertThat(loadedData[4].itemId).isEqualTo(106L)
        assertThat(loadedData[5].itemId).isEqualTo(101L)
    }

    @Test
    fun verifyDeleteItemById() = mainCoroutineRule.runBlockingTest {
        insertTestFoodItems()
        val itemsBeforeDelete =
            localDataSource.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()
        assertThat(itemsBeforeDelete is DataResult.Success).isTrue()

        val beforeDelete = (itemsBeforeDelete as DataResult.Success).data.firstOrNull {
            it.itemId == 101L
        }
        assertThat(beforeDelete).isNotNull()

        localDataSource.deleteFoodItemById(101L)

        val itemsAfterDelete =
            localDataSource.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()
        assertThat(itemsAfterDelete is DataResult.Success).isTrue()

        val afterDelete = (itemsAfterDelete as DataResult.Success).data.firstOrNull {
            it.itemId == 101L
        }
        assertThat(afterDelete).isNull()
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
        localDataSource.insertFoodItem(foodItem)
    }
}