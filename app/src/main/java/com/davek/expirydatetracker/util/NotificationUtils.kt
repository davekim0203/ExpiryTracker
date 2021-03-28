package com.davek.expirydatetracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.davek.expirydatetracker.MainActivity
import com.davek.expirydatetracker.R
import com.davek.expirydatetracker.receiver.AlarmReceiver

private const val SUMMARY_ID = 0
private const val NOTIFICATION_GROUP = "com.davek.expirydatetracker.NOTIFICATION_GROUP"

fun sendNotification(context: Context, title: String, messageBody: String, notificationId: Int) {
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder =
        NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(NOTIFICATION_GROUP)
            .build()

    val summaryNotification =
        NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_summary_title))
            .setContentText(context.getString(R.string.notification_summary_body))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setGroup(NOTIFICATION_GROUP)
            .setGroupSummary(true)
            .build()

    NotificationManagerCompat.from(context).apply {
        notify(notificationId, builder)
        notify(SUMMARY_ID, summaryNotification)
    }
}

fun cancelAlarm(context: Context, foodId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notifyIntent = Intent(context, AlarmReceiver::class.java)
    val notifyPendingIntent = PendingIntent.getBroadcast(
        context,
        foodId,
        notifyIntent,
        PendingIntent.FLAG_NO_CREATE
    )

    notifyPendingIntent?.let {
        alarmManager.cancel(it)
    }
}