package com.davek.expirydatetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    setShowBadge(true)
                    enableLights(true)
                    lightColor = Color.CYAN
                    enableVibration(true)
                    description = getString(R.string.notification_channel_description)
                }

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}