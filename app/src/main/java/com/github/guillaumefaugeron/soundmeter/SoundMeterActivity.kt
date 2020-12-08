package com.github.guillaumefaugeron.soundmeter

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.slider.Slider
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
class SoundMeterActivity: AppCompatActivity() {

    companion object {
        const val POPUP_DISPLAYED = "popupAlreadyDisplayed"
        const val TAG_PERMISSION_FRAGMENT = "permissionDialogFragment"
        const val REQUEST_CODE_PERMISSION = 1
        const val LOCATION_UPDATE_MS = 1_000L
        const val LOCATION_UPDATE_RADIUS = 100f
    }

    private var popupAlreadyDisplayed = false
    private var speed: Float = 50.0f

    private lateinit var startStopBtn: Button
    private lateinit var latitudeView: TextView
    private lateinit var longitudeView: TextView
    private lateinit var speedView: TextView


    //    private lateinit var speedView: TextView
    private lateinit var slider: Slider


    val locationConsumer = Consumer<Location> { location ->
        latitudeView.text = String.format("Latitude %.6f",location.latitude)
        longitudeView.text = String.format("Longitude %.6f",location.longitude)

        speedView.text = String.format("Speed : %.6f",location.speed, " m/s")

        slider.addOnChangeListener { slider, value, fromUser ->
            speed = value
            System.out.println(value)
        }


        if (location.speed > speed) {
            // threshold reached
            (getSystemService(VIBRATOR_SERVICE) as Vibrator).apply {
                vibrate(500)
            }
            Toast.makeText(this, "too fast", Toast.LENGTH_SHORT).show()
        }
    }

    var locationDisposable: Disposable? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_meter)
        popupAlreadyDisplayed = savedInstanceState?.getBoolean(POPUP_DISPLAYED) ?: false
        latitudeView = findViewById(R.id.latitude)
        longitudeView = findViewById(R.id.longitude)
        speedView = findViewById(R.id.speed)
        slider = findViewById(R.id.speed2)
        speed = slider.value
        startStopBtn = findViewById(R.id.start)
        startStopBtn.setOnClickListener {
            LocationService.startOrStop(this)
            syncUI() // TODO
        }
        popupAlreadyDisplayed = savedInstanceState?.getBoolean(POPUP_DISPLAYED) ?: false
        speedView = findViewById(R.id.speed)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            if (supportFragmentManager.findFragmentByTag(TAG_PERMISSION_FRAGMENT) == null
                    && !popupAlreadyDisplayed) {
                supportFragmentManager
                        .beginTransaction()
                        .add(PermissionPopupFragment(), TAG_PERMISSION_FRAGMENT)
                        .commit()
                popupAlreadyDisplayed = true
            }
        }
        syncUI()
        locationDisposable = LocationService.locationObservable.subscribe(locationConsumer)

    }

    override fun onPause() {
        super.onPause()
        locationDisposable?.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(POPUP_DISPLAYED, popupAlreadyDisplayed)
    }



//    @SuppressLint("MissingPermission")
//    private fun startOrStop() {
//        if (locationManager == null) {
//            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//            locationManager?.requestLocationUpdates(
//                    LOCATION_UPDATE_MS,
//                    LOCATION_UPDATE_RADIUS,
//                    Criteria().apply {
//                        accuracy = Criteria.ACCURACY_FINE
//                    }, locationListener, Looper.myLooper()
//            )
//        } else {
//            locationManager?.removeUpdates(locationListener)
//            locationManager = null
//            speedView.text = String.format("Speed : tracking stopped")
//
//        }
//        syncUI()
//    }

    private fun syncUI() {
        if (LocationService.isRunning) {
            startStopBtn.text = getString(R.string.btn_stop)
        } else {
            startStopBtn.text = getString(R.string.btn_start)
        }
        startStopBtn.isEnabled =
                ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

}