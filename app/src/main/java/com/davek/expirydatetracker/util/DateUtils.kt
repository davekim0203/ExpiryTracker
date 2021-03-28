package com.davek.expirydatetracker.util

import java.util.*
import java.util.concurrent.TimeUnit

fun getCalendarInstance(): Calendar = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun getRemainingDays(firstDayInMillis: Long, secondDayInMillis: Long): Int = try {
    TimeUnit.DAYS.convert(firstDayInMillis - secondDayInMillis, TimeUnit.MILLISECONDS)
} catch (e: Exception) {
    e.printStackTrace()
    0
}.toInt()