package com.davek.expirydatetracker.util

import com.davek.expirydatetracker.viewmodel.ItemDetailViewModel.Companion.DAY_IN_MILLI
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class DateUtilsTest {

    @Test
    fun verifyGetCalendarInstance() {
        val actual = getCalendarInstance()
        val actualYear = actual.get(Calendar.YEAR)
        val actualMonth = actual.get(Calendar.MONTH)
        val actualDay = actual.get(Calendar.DAY_OF_MONTH)
        val actualHour = actual.get(Calendar.HOUR_OF_DAY)
        val actualMinute = actual.get(Calendar.MINUTE)
        val actualSecond = actual.get(Calendar.SECOND)

        val expected = Calendar.getInstance()
        val expectedYear = expected.get(Calendar.YEAR)
        val expectedMonth = expected.get(Calendar.MONTH)
        val expectedDay = expected.get(Calendar.DAY_OF_MONTH)
        val expectedHour = 0
        val expectedMinute = 0
        val expectedSecond = 0

        assertEquals(expectedYear, actualYear)
        assertEquals(expectedMonth, actualMonth)
        assertEquals(expectedDay, actualDay)
        assertEquals(expectedHour, actualHour)
        assertEquals(expectedMinute, actualMinute)
        assertEquals(expectedSecond, actualSecond)
    }

    @Test
    fun verifyGetRemainingDays() {
        val expected = 5
        val date1 = getCalendarInstance().timeInMillis
        val date2 = date1 - DAY_IN_MILLI * expected
        val actual = getRemainingDays(date1, date2)

        assertEquals(expected, actual)
    }
}