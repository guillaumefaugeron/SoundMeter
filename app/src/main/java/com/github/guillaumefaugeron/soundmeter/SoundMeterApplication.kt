package com.github.guillaumefaugeron.soundmeter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class SoundMeterApplication: Application() {

    companion object {
        const val NOTIFICATION_CHANNEL = "service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            (getSystemService(NotificationManager::class.java) as NotificationManager)
                    .createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL, "channel name", NotificationManager.IMPORTANCE_HIGH))
        }
    }

}
