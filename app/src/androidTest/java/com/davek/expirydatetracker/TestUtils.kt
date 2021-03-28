package com.davek.expirydatetracker

import android.app.Activity
import android.widget.DatePicker
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription(): String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}

fun selectDateFromPicker(year: Int, month: Int, dayOfMonth: Int) {
    onView(ViewMatchers.withId(R.id.tvbt_expiry_date)).perform(click())
    onView(ViewMatchers.withClassName(CoreMatchers.equalTo(DatePicker::class.java.name))).perform(
        PickerActions.setDate(year, month, dayOfMonth)
    )
    onView(ViewMatchers.withId(android.R.id.button1))
        .inRoot(isDialog())
        .check(matches(ViewMatchers.withText("OK")))
        .check(matches(ViewMatchers.isDisplayed()))
        .perform(click())
}
