package com.davek.expirydatetracker.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.davek.expirydatetracker.MainCoroutineRule
import com.davek.expirydatetracker.getOrAwaitValue
import com.davek.expirydatetracker.util.SortType
import com.davek.expirydatetracker.util.getCalendarInstance
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultFoodItemRepositoryTest {

    private val item1 = getFoodItemWithFields(101L, "Bacon", 2020, 6, 18, 6)
    private val item2 = getFoodItemWithFields(102L, "Milk", 2020, 6, 8, 5)
    private val item3 = getFoodItemWithFields(103L, "Ham", 2020, 5, 8, 4)
    private val item4 = getFoodItemWithFields(104L, "Juice", 2019, 10, 8, 3)
    private val item5 = getFoodItemWithFields(105L, "Cookies", 2019, 10, 7, 2)
    private val item6 = getFoodItemWithFields(106L, "Butter", 2018, 6, 8, 1)
    private val items = listOf(item1, item2, item3, item4, item5, item6)
    private lateinit var repository: DefaultFoodItemRepository
    private lateinit var foodItemLocalDataSource: FakeDataSource

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
        foodItemLocalDataSource = FakeDataSource(items.toMutableList())
        repository = DefaultFoodItemRepository(foodItemLocalDataSource, Dispatchers.Main)
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
        repository.insertFoodItem(foodItem)
        val loaded = repository.getFoodItemById(foodItem.itemId)

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
        repository.insertFoodItem(foodItem)

        foodItem.apply {
            name = "Updated name"
            expiryDate = getCalendarInstance().apply { set(1999, 2, 22) }.time
            quantity = 2
            isNotificationOn = false
            note = null
        }

        repository.updateFoodItem(foodItem)
        val loaded = repository.getFoodItemById(foodItem.itemId)

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.REMAINING_DAYS_DESC).getOrAwaitValue()

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.QUANTITY_ASC).getOrAwaitValue()

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.QUANTITY_DESC).getOrAwaitValue()

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.NAME_ASC).getOrAwaitValue()

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
        val allFoodItems =
            repository.observeSortedFoodItems(SortType.NAME_DESC).getOrAwaitValue()

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
        val itemsBeforeDelete =
            repository.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()
        assertThat(itemsBeforeDelete is DataResult.Success).isTrue()

        val beforeDelete = (itemsBeforeDelete as DataResult.Success).data.firstOrNull {
            it.itemId == 101L
        }
        assertThat(beforeDelete).isNotNull()

        repository.deleteFoodItemById(101L)

        val itemsAfterDelete =
            repository.observeSortedFoodItems(SortType.REMAINING_DAYS_ASC).getOrAwaitValue()
        assertThat(itemsAfterDelete is DataResult.Success).isTrue()

        val afterDelete = (itemsAfterDelete as DataResult.Success).data.firstOrNull {
            it.itemId == 101L
        }
        assertThat(afterDelete).isNull()
    }

    private fun getFoodItemWithFields(
        id: Long,
        name: String,
        year: Int, month: Int, date: Int,
        quantity: Int
    ): FoodItem {
        val expiryDate = getCalendarInstance().apply { set(year, month, date) }
        return FoodItem(id, name, expiryDate.time, quantity)
    }
}