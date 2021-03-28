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
import com.davek.expirydatetracker.util.cancelAlarm
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.DEFAULT_NOTIFICATION_STATUS
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.DEFAULT_QUANTITY
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.SNACKBAR_ID_ITEM_DELETED
import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.SNACKBAR_ID_NAME_REQUIRED
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_ITEM_ID
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ItemDetailViewModelTest {

    private lateinit var itemDetailViewModel: ItemDetailViewModel
    private lateinit var foodItemRepository: FakeRepository
    private lateinit var testDate: Date

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun before() {
        foodItemRepository = FakeRepository()
        testDate = getCalendarInstance().apply { set(2010, 2, 4) }.time
        val item1 = FoodItem(1L, "Food 1", getCalendarInstance().time, 1, true, null)
        val item2 = FoodItem(2L, "Food 2", testDate, 2, false, "Food 2 comment")
        val item3 = FoodItem(3L, "Food 3", getCalendarInstance().time, 99, true, null)
        foodItemRepository.addFoodItems(item1, item2, item3)

        itemDetailViewModel = ItemDetailViewModel(getApplicationContext(), foodItemRepository)
    }

    @Test
    fun verifyNewFoodItem() {
        itemDetailViewModel.loadItem(DEFAULT_ITEM_ID)

        assertThat(itemDetailViewModel.isNotificationOn.getOrAwaitValue()).isEqualTo(
            DEFAULT_NOTIFICATION_STATUS
        )
        assertThat(itemDetailViewModel.expiryDate.getOrAwaitValue()).isEqualTo(
            getCalendarInstance().time
        )
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(
            DEFAULT_QUANTITY
        )
    }

    @Test
    fun verifyLoadFoodItem() {
        itemDetailViewModel.loadItem(2L)

        assertThat(itemDetailViewModel.foodName.getOrAwaitValue()).isEqualTo("Food 2")
        assertThat(itemDetailViewModel.expiryDate.getOrAwaitValue()).isEqualTo(testDate)
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(2)
        assertThat(itemDetailViewModel.isNotificationOn.getOrAwaitValue()).isEqualTo(false)
        assertThat(itemDetailViewModel.note.getOrAwaitValue()).isEqualTo("Food 2 comment")
    }

    @Test
    fun verifyQuantityIncreaseButtonClick() {
        itemDetailViewModel.loadItem(3L)

        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(99)
        itemDetailViewModel.onQuantityIncreaseButtonClick()
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(100)
        itemDetailViewModel.onQuantityIncreaseButtonClick()
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(100)
    }

    @Test
    fun verifyQuantityDecreaseButtonClick() {
        itemDetailViewModel.loadItem(1L)

        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(1)
        itemDetailViewModel.onQuantityDecreaseButtonClick()
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(0)
        itemDetailViewModel.onQuantityDecreaseButtonClick()
        assertThat(itemDetailViewModel.quantity.getOrAwaitValue()).isEqualTo(0)
    }

    @Test
    fun verifyDateClick() {
        itemDetailViewModel.loadItem(2L)

        val observer = mock(Observer::class.java) as Observer<Date>
        itemDetailViewModel.showDatePicker.observeForever(observer)
        itemDetailViewModel.onDateClick()

        verify(observer).onChanged(testDate)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyDateSelected() {
        val expected = getCalendarInstance().apply { set(2012, 10, 21) }.time
        itemDetailViewModel.loadItem(2L)
        itemDetailViewModel.onDateSelected(2012, 10, 21)

        assertThat(itemDetailViewModel.expiryDate.getOrAwaitValue()).isEqualTo(expected)
    }

    @Test
    fun verifyNotificationToggleChanged() {
        val expected = false
        val observer = mock(Observer::class.java) as Observer<Boolean>
        itemDetailViewModel.isNotificationOn.observeForever(observer)
        itemDetailViewModel.onNotificationToggleChanged(expected)

        verify(observer).onChanged(expected)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyRequiredFieldsWarningSnackbar() {
        itemDetailViewModel.loadItem(DEFAULT_ITEM_ID)
        val snackbarObserver = mock(Observer::class.java) as Observer<Int>
        val navigationObserver = mock(Observer::class.java) as Observer<Any>
        itemDetailViewModel.showSnackbar.observeForever(snackbarObserver)
        itemDetailViewModel.navigateToItemList.observeForever(navigationObserver)

        // When name is null, show snackbar
        itemDetailViewModel.onSaveButtonClick()
        verify(snackbarObserver).onChanged(SNACKBAR_ID_NAME_REQUIRED)
        verifyNoMoreInteractions(snackbarObserver)
        verifyNoMoreInteractions(navigationObserver)

        // When name is blank, show snackbar
        itemDetailViewModel.foodName.value = ""
        itemDetailViewModel.onSaveButtonClick()
        verify(snackbarObserver, times(2)).onChanged(SNACKBAR_ID_NAME_REQUIRED)
        verifyNoMoreInteractions(snackbarObserver)
        verifyNoMoreInteractions(navigationObserver)

        // When name is valid, do not show snackbar
        itemDetailViewModel.foodName.value = "Valid Name"
        itemDetailViewModel.onSaveButtonClick()
        verifyNoMoreInteractions(snackbarObserver)
        verify(navigationObserver).onChanged(any())
        verifyNoMoreInteractions(navigationObserver)
    }

    @Test
    fun verifySaveNewItem() {
        val newName = "New Food"
        val newExpiryDate = getCalendarInstance().apply { set(2013, 6, 14) }.time
        val newNotificationStatus = false
        val newNote = "New Note"

        // Start new item and fill out fields
        itemDetailViewModel.apply {
            loadItem(DEFAULT_ITEM_ID)
            foodName.value = newName
            onDateSelected(2013, 6, 14)
            onQuantityIncreaseButtonClick()
            onNotificationToggleChanged(newNotificationStatus)
            note.value = newNote
        }

        val observer = mock(Observer::class.java) as Observer<Any>
        itemDetailViewModel.navigateToItemList.observeForever(observer)

        // Check new item is not in database
        assertThat(foodItemRepository.foodItems.any { it.value.name == newName }).isFalse()

        // Save
        itemDetailViewModel.onSaveButtonClick()

        // Check new item is in database
        assertThat(foodItemRepository.foodItems.any { it.value.name == newName }).isTrue()
        val savedItem = foodItemRepository.foodItems.filter {
            it.value.name == newName
        }.values.first()

        // Check all fields
        assertThat(savedItem.name).isEqualTo(newName)
        assertThat(savedItem.expiryDate).isEqualTo(newExpiryDate)
        assertThat(savedItem.quantity).isEqualTo(
            2       //2 because default value is 1 and increased once
        )
        assertThat(savedItem.isNotificationOn).isEqualTo(newNotificationStatus)
        assertThat(savedItem.note).isEqualTo(newNote)

        verify(observer).onChanged(any())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyUpdateItem() = mainCoroutineRule.runBlockingTest {
        val updatedName = "Updated Food"
        val updatedExpiryDate = getCalendarInstance().apply { set(2018, 5, 22) }.time
        val updatedNotificationStatus = true
        val updatedNote = "Updated Note"

        itemDetailViewModel.loadItem(2L)
        val observer = mock(Observer::class.java) as Observer<Any>
        itemDetailViewModel.navigateToItemList.observeForever(observer)

        // Check loaded item
        val dataFromDb = foodItemRepository.getFoodItemById(2L)
        assertThat((dataFromDb as DataResult.Success).data.itemId).isEqualTo(2L)
        assertThat(dataFromDb.data.name).isEqualTo("Food 2")
        assertThat(dataFromDb.data.expiryDate).isEqualTo(testDate)
        assertThat(dataFromDb.data.quantity).isEqualTo(2)
        assertThat(dataFromDb.data.isNotificationOn).isFalse()
        assertThat(dataFromDb.data.note).isEqualTo("Food 2 comment")

        // Update loaded item
        itemDetailViewModel.apply {
            foodName.value = updatedName
            onDateSelected(2018, 5, 22)
            onQuantityDecreaseButtonClick()
            onNotificationToggleChanged(updatedNotificationStatus)
            note.value = updatedNote
        }

        // Save
        itemDetailViewModel.onSaveButtonClick()

        // Check updated item
        val dataAfterUpdate = foodItemRepository.getFoodItemById(2L)
        assertThat((dataAfterUpdate as DataResult.Success).data.itemId).isEqualTo(2L)
        assertThat(dataAfterUpdate.data.name).isEqualTo(updatedName)
        assertThat(dataAfterUpdate.data.expiryDate).isEqualTo(updatedExpiryDate)
        assertThat(dataAfterUpdate.data.quantity).isEqualTo(
            1       // 1 because loaded value is 2 and decreased once
        )
        assertThat(dataAfterUpdate.data.isNotificationOn).isEqualTo(updatedNotificationStatus)
        assertThat(dataAfterUpdate.data.note).isEqualTo(updatedNote)

        verify(observer).onChanged(any())
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun verifyCancelAlarmWhenTurnOffNotification() {
        mockkStatic("com.davek.expirydatetracker.util.NotificationUtilsKt")
        every { cancelAlarm(any(), any()) } returns Unit

        itemDetailViewModel.loadItem(1L)
        itemDetailViewModel.onNotificationToggleChanged(false)
        itemDetailViewModel.onSaveButtonClick()

        io.mockk.verify(exactly = 1) {
            cancelAlarm(getApplicationContext(), 1)
        }

        clearAllMocks()
    }

    @Test
    fun verifyDeleteFoodItem() = mainCoroutineRule.runBlockingTest {
        val itemIdToDelete = 1L
        itemDetailViewModel.loadItem(itemIdToDelete)

        mockkStatic("com.davek.expirydatetracker.util.NotificationUtilsKt")
        every { cancelAlarm(any(), any()) } returns Unit
        val showSnackbarObserver = mock(Observer::class.java) as Observer<Int>
        val navigationObserver = mock(Observer::class.java) as Observer<Any>
        itemDetailViewModel.showSnackbar.observeForever(showSnackbarObserver)
        itemDetailViewModel.showSnackbar.observeForever(navigationObserver)

        // Check FoodItem with id of itemIdToDelete is in database
        assertThat(foodItemRepository.getFoodItemById(itemIdToDelete) is DataResult.Success).isTrue()

        // Delete the item
        itemDetailViewModel.deleteFoodItem(getApplicationContext())

        // Check FoodItem with id of itemIdToDelete is not in database anymore
        assertThat(foodItemRepository.getFoodItemById(itemIdToDelete) is DataResult.Error).isTrue()

        io.mockk.verify(exactly = 1) {
            cancelAlarm(getApplicationContext(), 1)
        }
        verify(showSnackbarObserver).onChanged(SNACKBAR_ID_ITEM_DELETED)
        verify(navigationObserver).onChanged(SNACKBAR_ID_ITEM_DELETED)
        verifyNoMoreInteractions(showSnackbarObserver)
        verifyNoMoreInteractions(navigationObserver)

        clearAllMocks()
    }
}