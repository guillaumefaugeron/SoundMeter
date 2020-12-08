package com.github.guillaumefaugeron.soundmeter

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class LocationService: Service() {

    companion object {
        const val LOCATION_ACTION = "com.github.avianey.soundmeter.LOCATION_ACTION"
        val EMPTY_LOCATION = Location("")

        fun startOrStop(context: Context) {
            Intent(context, LocationService::class.java).let { intent ->
                if (isRunning) {
                    context.stopService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        var isRunning = false
            private set

        var locationObservable = BehaviorSubject.createDefault(EMPTY_LOCATION)
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationReceiver: BroadcastReceiver

    // region lifecycle

    override fun onBind(intent: Intent?): IBinder? {
        throw IllegalStateException("Should not be bound")
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationReceiver = LocationBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceivers()
        requestLocationUpdates()
        startForeground(42, getPersistentNotification())
        isRunning = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        unregisterReceivers()
        stopForeground(true)
        isRunning = false
    }

    // endregion

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        locationManager.requestLocationUpdates(
                SoundMeterActivity.LOCATION_UPDATE_MS,
                SoundMeterActivity.LOCATION_UPDATE_RADIUS,
                Criteria().apply {
                    accuracy = Criteria.ACCURACY_FINE
                }, getLocationPendingIntent())
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(getLocationPendingIntent())
    }

    private fun registerReceivers() {
        registerReceiver(locationReceiver, IntentFilter(LOCATION_ACTION))
    }

    private fun unregisterReceivers() {
        unregisterReceiver(locationReceiver)
    }

    private fun getPersistentNotification() =
            NotificationCompat.Builder(this, SoundMeterApplication.NOTIFICATION_CHANNEL)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentIntent(getActivityPendingIntent())
                    .setColorized(true)
                    .setColor(resources.getColor(R.color.purple_500))
                    .build()

    private fun getLocationPendingIntent() =
            PendingIntent.getBroadcast(this, 0,
                    Intent(LOCATION_ACTION), PendingIntent.FLAG_UPDATE_CURRENT)

    private fun getActivityPendingIntent() =
            PendingIntent.getActivity(this, 0,
                    Intent(this, SoundMeterActivity::class.java),
                    PendingIntent.FLAG_CANCEL_CURRENT)


    private inner class LocationBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.getParcelable<Location>(LocationManager.KEY_LOCATION_CHANGED)?.let { location ->
                locationObservable.onNext(location)
            }
        }
    }


}