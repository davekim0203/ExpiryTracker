package com.davek.expirydatetracker.viewmodel

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davek.expirydatetracker.MainCoroutineRule
import com.davek.expirydatetracker.database.DataResult
import com.davek.expirydatetracker.database.FakeRepository
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.getOrAwaitValue
import com.davek.expirydatetracker.util.SortType
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_SORT_TYPE
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ItemListViewModelTest {

    private lateinit var itemListViewModel: ItemListViewModel
    private lateinit var foodItemRepository: FakeRepository

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        foodItemRepository = FakeRepository()
        val item1 = getFoodItemWithFields(1L, "Bacon", 2020, 6, 18, 6)
        val item2 = getFoodItemWithFields(2L, "Milk", 2020, 6, 8, 5)
        val item3 = getFoodItemWithFields(3L, "Ham", 2020, 5, 8, 4)
        val item4 = getFoodItemWithFields(4L, "Juice", 2019, 10, 8, 3)
        val item5 = getFoodItemWithFields(5L, "Cookies", 2019, 10, 7, 2)
        val item6 = getFoodItemWithFields(6L, "Butter", 2018, 6, 8, 1)
        foodItemRepository.addFoodItems(item1, item2, item3, item4, item5, item6)

        itemListViewModel = ItemListViewModel(foodItemRepository)
    }

    @Test
    fun verifyInit() {
        val expected = DEFAULT_SORT_TYPE
        val observer = mock(Observer::class.java) as Observer<SortType>
        itemListViewModel.sortType.observeForever(observer)

        verify(observer).onChanged(expected)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifySetSortType() {
        val expected = SortType.QUANTITY_ASC
        val observer = mock(Observer::class.java) as Observer<SortType>
        itemListViewModel.sortType.observeForever(observer)

        verify(observer).onChanged(DEFAULT_SORT_TYPE)
        verifyNoMoreInteractions(observer)

        itemListViewModel.setSortType(expected.value)

        verify(observer).onChanged(expected)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifySortItemsByRemainingDaysAsc() {
        itemListViewModel.setSortType(SortType.REMAINING_DAYS_ASC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(6L)
        assertThat(sortedItems[1].itemId).isEqualTo(5L)
        assertThat(sortedItems[2].itemId).isEqualTo(4L)
        assertThat(sortedItems[3].itemId).isEqualTo(3L)
        assertThat(sortedItems[4].itemId).isEqualTo(2L)
        assertThat(sortedItems[5].itemId).isEqualTo(1L)
    }

    @Test
    fun verifySortItemsByRemainingDaysDesc() {
        itemListViewModel.setSortType(SortType.REMAINING_DAYS_DESC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(1L)
        assertThat(sortedItems[1].itemId).isEqualTo(2L)
        assertThat(sortedItems[2].itemId).isEqualTo(3L)
        assertThat(sortedItems[3].itemId).isEqualTo(4L)
        assertThat(sortedItems[4].itemId).isEqualTo(5L)
        assertThat(sortedItems[5].itemId).isEqualTo(6L)
    }

    @Test
    fun verifySortItemsByQuantityAsc() {
        itemListViewModel.setSortType(SortType.QUANTITY_ASC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(6L)
        assertThat(sortedItems[1].itemId).isEqualTo(5L)
        assertThat(sortedItems[2].itemId).isEqualTo(4L)
        assertThat(sortedItems[3].itemId).isEqualTo(3L)
        assertThat(sortedItems[4].itemId).isEqualTo(2L)
        assertThat(sortedItems[5].itemId).isEqualTo(1L)
    }

    @Test
    fun verifySortItemsByQuantityDesc() {
        itemListViewModel.setSortType(SortType.QUANTITY_DESC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(1L)
        assertThat(sortedItems[1].itemId).isEqualTo(2L)
        assertThat(sortedItems[2].itemId).isEqualTo(3L)
        assertThat(sortedItems[3].itemId).isEqualTo(4L)
        assertThat(sortedItems[4].itemId).isEqualTo(5L)
        assertThat(sortedItems[5].itemId).isEqualTo(6L)
    }

    @Test
    fun verifySortItemsByNameAsc() {
        itemListViewModel.setSortType(SortType.NAME_ASC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(1L)
        assertThat(sortedItems[1].itemId).isEqualTo(6L)
        assertThat(sortedItems[2].itemId).isEqualTo(5L)
        assertThat(sortedItems[3].itemId).isEqualTo(3L)
        assertThat(sortedItems[4].itemId).isEqualTo(4L)
        assertThat(sortedItems[5].itemId).isEqualTo(2L)
    }

    @Test
    fun verifySortItemsByNameDesc() {
        itemListViewModel.setSortType(SortType.NAME_DESC.value)
        val sortedItems = itemListViewModel.foodItems.getOrAwaitValue()
        assertThat(sortedItems[0].itemId).isEqualTo(2L)
        assertThat(sortedItems[1].itemId).isEqualTo(4L)
        assertThat(sortedItems[2].itemId).isEqualTo(3L)
        assertThat(sortedItems[3].itemId).isEqualTo(5L)
        assertThat(sortedItems[4].itemId).isEqualTo(6L)
        assertThat(sortedItems[5].itemId).isEqualTo(1L)
    }

    @Test
    fun verifyInsertFoodItem() = mainCoroutineRule.runBlockingTest {
        val newItem1 = FoodItem(7L, "Food 7", getCalendarInstance().time, 7)
        val newItem2 = FoodItem(8L, "Food 8", getCalendarInstance().time, 8)

        assertThat(itemListViewModel.foodItems.getOrAwaitValue()).hasSize(6)
        foodItemRepository.addFoodItems(newItem1, newItem2)
        assertThat(itemListViewModel.foodItems.getOrAwaitValue()).hasSize(8)
    }

    @Test
    fun verifyAddButtonClick() {
        val observer = mock(Observer::class.java) as Observer<Long>
        itemListViewModel.navigateToItemDetail.observeForever(observer)
        itemListViewModel.onAddButtonClick()

        verify(observer).onChanged(ItemListViewModel.DEFAULT_ITEM_ID)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyListItemClick() {
        val expected = 10L
        val observer = mock(Observer::class.java) as Observer<Long>
        itemListViewModel.navigateToItemDetail.observeForever(observer)
        itemListViewModel.onFoodItemClick(expected)

        verify(observer).onChanged(expected)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyDeleteFoodItem() = mainCoroutineRule.runBlockingTest {
        val itemIdToDelete = 2L

        val observer = mock(Observer::class.java) as Observer<Any>
        itemListViewModel.showSnackbar.observeForever(observer)

        // Check FoodItem with id of itemIdToDelete is in database
        assertThat(foodItemRepository.getFoodItemById(itemIdToDelete) is DataResult.Success).isTrue()

        // Delete the item
        itemListViewModel.deleteFoodItem(getApplicationContext(), itemIdToDelete)

        // Check FoodItem with id of itemIdToDelete is not in database anymore
        assertThat(foodItemRepository.getFoodItemById(itemIdToDelete) is DataResult.Error).isTrue()

        verify(observer).onChanged(any())
        verifyNoMoreInteractions(observer)
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