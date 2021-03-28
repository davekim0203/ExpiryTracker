package com.davek.expirydatetracker

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davek.expirydatetracker.adapter.ItemAdapter
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import com.davek.expirydatetracker.di.FoodItemRepositoryModule
import com.davek.expirydatetracker.util.getCalendarInstance
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@UninstallModules(FoodItemRepositoryModule::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FoodItemRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun verifyAddNewItemToolbar() {
        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.add_floating_action_button)).perform(click())
        onView(withId(R.id.menu_delete)).check(doesNotExist())
        onView(withId(R.id.menu_save)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.add_item_title))))

        activityScenario.close()
    }

    @Test
    fun verifyEditItemToolbar() {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 3
        )
        repository.insertItemBlocking(foodItem)

        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(0, click())
        )

        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()))
        onView(withId(R.id.menu_save)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.edit_item_title))))

        activityScenario.close()
    }

    @Test
    fun verifyAddNewItem() {
        val expectedTitle = "New Food"
        val activityScenario = launch(MainActivity::class.java)

        onView(withText(expectedTitle)).check(doesNotExist())

        onView(withId(R.id.add_floating_action_button)).perform(click())
        onView(withId(R.id.et_food_name)).perform(replaceText(expectedTitle))
        onView(withId(R.id.menu_save)).perform(click())

        onView(withText(expectedTitle)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun verifyRequiredFieldSnackbar() {
        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.add_floating_action_button)).perform(click())
        onView(withId(R.id.menu_save)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.name_required_snackbar_message)))

        activityScenario.close()
    }

    @Test
    fun verifyEditItem() {
        val foodItem = FoodItem(
            100L,
            "Food 1",
            getCalendarInstance().apply { set(2010, 8, 12) }.time,
            2,
            false,
            "Any note"
        )
        repository.insertItemBlocking(foodItem)

        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(0, click())
        )

        // Verify food item is loaded successfully
        onView(withId(R.id.et_food_name)).check(matches(withText(foodItem.name)))
        onView(withId(R.id.tvbt_expiry_date)).check(matches(withText("2010-09-12")))
        onView(withId(R.id.et_note)).check(matches(withText(foodItem.note)))
        onView(withId(R.id.tv_quantity)).check(matches(withText("2")))
        onView(withId(R.id.switch_notification_days)).check(matches(not(isChecked())))

        // Edit the item
        onView(withId(R.id.et_food_name)).perform(replaceText("Title Changed"))
        selectDateFromPicker(2017, 11, 20)
        onView(withId(R.id.et_note)).perform(replaceText("Note Changed"))
        onView(withId(R.id.quantity_decrease_button)).perform(click())
        onView(withId(R.id.switch_notification_days)).perform(click())

        // Save
        onView(withId(R.id.menu_save)).perform(click())

        // Verify list item is successfully updated
        onView(withText("Food 1")).check(doesNotExist())
        onView(withText("Title Changed")).check(matches(isDisplayed()))

        // Verify all other fields are successfully updated
        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(0, click())
        )
        onView(withId(R.id.et_food_name)).check(matches(withText("Title Changed")))
        onView(withId(R.id.tvbt_expiry_date)).check(matches(withText("2017-11-20")))
        onView(withId(R.id.et_note)).check(matches(withText("Note Changed")))
        onView(withId(R.id.tv_quantity)).check(matches(withText("1")))
        onView(withId(R.id.switch_notification_days)).check(matches(isChecked()))

        activityScenario.close()
    }

    @Test
    fun verifyDeleteItemFromDetailFragment() {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 3
        )
        repository.insertItemBlocking(foodItem)

        val activityScenario = launch(MainActivity::class.java)

        onView(withText("Food 1")).check(matches(isDisplayed()))

        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(0, click())
        )
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .check(matches(withText(R.string.delete_food_item_confirm_dialog_positive_button)))
            .check(matches(isDisplayed()))
            .perform(click())

        // Check if navigated to ItemListFragment with snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.item_deleted_snackbar_message)))
        onView(withId(R.id.food_item_list)).check(matches(isDisplayed()))
        onView(withText("Food 1")).check(doesNotExist())

        activityScenario.close()
    }

    @Test
    fun verifyBackArrowButtonToNavigateToItemListFragment() {
        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.add_floating_action_button)).perform(click())
        onView(withId(R.id.quantity_decrease_button)).check(matches(isDisplayed()))
        onView(
            withContentDescription(
                activityScenario.getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.food_item_list)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun verifySystemBackButtonToNavigateToItemListFragment() {
        val activityScenario = launch(MainActivity::class.java)

        onView(withId(R.id.add_floating_action_button)).perform(click())
        onView(withId(R.id.quantity_decrease_button)).check(matches(isDisplayed()))
        pressBack()
        onView(withId(R.id.food_item_list)).check(matches(isDisplayed()))

        activityScenario.close()
    }
}