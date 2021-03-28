package com.davek.expirydatetracker.fragment

import android.content.Context
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davek.expirydatetracker.MainActivity
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.adapter.ItemAdapter
import com.davek.expirydatetracker.database.FoodItem
import com.davek.expirydatetracker.database.FoodItemRepository
import com.davek.expirydatetracker.di.FoodItemRepositoryModule
import com.davek.expirydatetracker.insertItemBlocking
import com.davek.expirydatetracker.launchFragmentInHiltContainer
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@UninstallModules(FoodItemRepositoryModule::class)
@HiltAndroidTest
class ItemListFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FoodItemRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun verifyListItem() {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 3
        )
        repository.insertItemBlocking(foodItem)

        launchActivity()

        onView(withText("Food 1")).check(matches(isDisplayed()))
    }

    @Test
    fun verifyDeleteItem() {
        repository.insertItemBlocking(FoodItem(101L, "Food 1", getCalendarInstance().time))
        repository.insertItemBlocking(FoodItem(102L, "Food 2", getCalendarInstance().time))

        launchActivity()

        onView(withText("Food 1")).check(matches(isDisplayed()))
        onView(withText("Food 2")).check(matches(isDisplayed()))

        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(
                0, GeneralSwipeAction(
                    Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                    Press.FINGER
                )
            )
        )
        onView(withText(R.string.delete_food_item_confirm_dialog_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .check(matches(withText(R.string.delete_food_item_confirm_dialog_positive_button)))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.item_deleted_snackbar_message)))

        onView(withText("Food 1")).check(doesNotExist())
        onView(withText("Food 2")).check(matches(isDisplayed()))

    }

    @Test
    fun verifyExpiredItemsStatusTextHiddenInitially() {
        launchActivity()

        onView(withId(R.id.tv_expired_times_count)).check(matches(not(isDisplayed())))
    }

    @Test
    fun verifyExpiredItemsStatusText() {
        val foodItem = FoodItem(
            10L,
            "Food 1",
            getCalendarInstance().apply { set(2000, 10, 22) }.time
        )
        repository.insertItemBlocking(foodItem)

        launchActivity()

        onView(withId(R.id.tv_expired_times_count)).check(matches(isDisplayed()))
    }

    @Test
    fun verifySortMenuAndDialog() {
        launchActivity()
        onView(withId(R.id.menu_sort)).perform(click())
        onView(withText(R.string.sort_dialog_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun verifyClickListItemToNavigateToItemDetailFragment() {
        val foodItem = FoodItem(
            itemId = 100L,
            name = "Food 1",
            expiryDate = getCalendarInstance().time,
            quantity = 3
        )
        repository.insertItemBlocking(foodItem)

        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ItemListFragment>(Bundle(), R.style.Theme_ExpiryDateTracker) {
            Navigation.setViewNavController(this.view!!, navController)
        }

        onView(withId(R.id.food_item_list)).perform(
            actionOnItemAtPosition<ItemAdapter.ViewHolder>(0, click())
        )

        verify(navController).navigate(
            ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(
                100L, getApplicationContext<Context>().getString(R.string.edit_item_title)
            )
        )
    }

    @Test
    fun verifyAddButtonToNavigateToItemDetailFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ItemListFragment>(Bundle(), R.style.Theme_ExpiryDateTracker) {
            Navigation.setViewNavController(this.view!!, navController)
        }

        onView(withId(R.id.add_floating_action_button)).perform(click())

        verify(navController).navigate(
            ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(
                DEFAULT_ITEM_ID, getApplicationContext<Context>().getString(R.string.add_item_title)
            )
        )
    }

    private fun launchActivity(): ActivityScenario<MainActivity>? {
        val activityScenario = launch(MainActivity::class.java)
        activityScenario.onActivity { activity ->
            (activity.findViewById(R.id.food_item_list) as RecyclerView).itemAnimator = null
        }
        return activityScenario
    }
}