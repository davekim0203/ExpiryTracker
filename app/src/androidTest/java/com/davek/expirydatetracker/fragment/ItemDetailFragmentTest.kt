package com.davek.expirydatetracker.fragment

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import com.davek.expirydatetracker.di.FoodItemRepositoryModule
import com.davek.expirydatetracker.insertItemBlocking
import com.davek.expirydatetracker.launchFragmentInHiltContainer
import com.davek.expirydatetracker.selectDateFromPicker
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.viewmodel.ItemListViewModel.Companion.DEFAULT_ITEM_ID
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@UninstallModules(FoodItemRepositoryModule::class)
@HiltAndroidTest
class ItemDetailFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FoodItemRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun verifyAddNewItem() {
        val expectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            getCalendarInstance().time
        )

        launchFragmentWithAddItem()

        onView(withId(R.id.et_food_name)).check(matches(withHint(R.string.hint_required_field)))
        onView(withId(R.id.tvbt_expiry_date)).check(matches(withText(expectedDate)))
        onView(withId(R.id.et_note)).check(matches(withHint(R.string.hint_optional_field)))
        onView(withId(R.id.tv_quantity)).check(matches(withText("1")))
        onView(withId(R.id.switch_notification_days)).check(matches(isChecked()))
    }

    @Test
    fun verifyLoadItemForEdit() {
        val foodItem = FoodItem(
            10L,
            "Food 1",
            getCalendarInstance().apply { set(2021, 10, 22) }.time,
            3,
            false,
            "Any note"
        )
        repository.insertItemBlocking(foodItem)

        val bundle = ItemDetailFragmentArgs(
            foodItem.itemId,
            getApplicationContext<Context>().getString(R.string.edit_item_title)
        ).toBundle()
        launchFragmentInHiltContainer<ItemDetailFragment>(bundle, R.style.Theme_ExpiryDateTracker)

        onView(withId(R.id.et_food_name)).check(matches(withText(foodItem.name)))
        onView(withId(R.id.tvbt_expiry_date)).check(matches(withText("2021-11-22")))
        onView(withId(R.id.et_note)).check(matches(withText(foodItem.note)))
        onView(withId(R.id.tv_quantity)).check(matches(withText("3")))
        onView(withId(R.id.switch_notification_days)).check(matches(not(isChecked())))
    }

    @Test
    fun verifyDatePicker() {
        launchFragmentWithAddItem()

        selectDateFromPicker(2022, 1, 20)

        onView(withId(R.id.tvbt_expiry_date)).check(matches(withText("2022-01-20")))
    }

    @Test
    fun verifyQuantityButtons() {
        launchFragmentWithAddItem()

        onView(withId(R.id.tv_quantity)).check(matches(withText("1")))
        onView(withId(R.id.quantity_increase_button)).perform(click())
        onView(withId(R.id.tv_quantity)).check(matches(withText("2")))
        onView(withId(R.id.quantity_decrease_button)).perform(click())
        onView(withId(R.id.tv_quantity)).check(matches(withText("1")))
        onView(withId(R.id.quantity_decrease_button)).perform(click())
        onView(withId(R.id.tv_quantity)).check(matches(withText("0")))
    }

    private fun launchFragmentWithAddItem() {
        val bundle = ItemDetailFragmentArgs(
            DEFAULT_ITEM_ID,
            getApplicationContext<Context>().getString(R.string.add_item_title)
        ).toBundle()
        launchFragmentInHiltContainer<ItemDetailFragment>(bundle, R.style.Theme_ExpiryDateTracker)
    }
}