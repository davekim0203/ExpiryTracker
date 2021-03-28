package com.davek.expirydatetracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.util.cancelAlarm
import com.davek.expirydatetracker.util.getCalendarInstance
import com.davek.expirydatetracker.util.getRemainingDays
import com.davek.expirydatetracker.util.sendNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val foodId = intent.getIntExtra("foodId", 0)
        val foodName = intent.getStringExtra("foodName")
        val expiryDateInMillis = intent.getLongExtra("expiryDate", 0)
        val remainingDays = getRemainingDays(expiryDateInMillis, getCalendarInstance().time.time)

        when {
            remainingDays < 0 -> {
                sendNotification(
                    context,
                    String.format(
                        context.getString(R.string.notification_expired_item_title),
                        foodName
                    ),
                    String.format(
                        context.getString(R.string.notification_expired_item_body),
                        foodName
                    ),
                    foodId
                )
                cancelAlarm(context, foodId)
            }
            remainingDays == 0 -> {
                sendNotification(
                    context,
                    String.format(
                        context.getString(R.string.notification_expires_today_item_title),
                        foodName
                    ),
                    String.format(
                        context.getString(R.string.notification_expires_today_item_body),
                        foodName
                    ),
                    foodId
                )
            }
            else -> {
                sendNotification(
                    context,
                    String.format(
                        context.getString(R.string.notification_expires_in_a_week_item_title),
                        foodName,
                        remainingDays
                    ),
                    String.format(
                        context.getString(R.string.notification_expires_in_a_week_item_body),
                        foodName,
                        remainingDays
                    ),
                    foodId
                )
            }
        }
    }
}