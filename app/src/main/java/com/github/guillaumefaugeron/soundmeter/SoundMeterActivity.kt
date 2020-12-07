package com.github.guillaumefaugeron.soundmeter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.util.Log.DEBUG
import android.util.Log.INFO
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.guillaumefaugeron.soundmeter.BuildConfig.DEBUG
import com.google.android.material.slider.Slider
import java.util.*

class SoundMeterActivity: AppCompatActivity() {

    companion object {
        const val POPUP_DISPLAYED = "popupAlreadyDisplayed"
        const val TAG_PERMISSION_FRAGMENT = "permissionDialogFragment"
        const val REQUEST_CODE_PERMISSION = 1
        const val LOCATION_UPDATE_MS = 1_000L
        const val LOCATION_UPDATE_RADIUS = 100f
    }

    private var popupAlreadyDisplayed = false
    private var locationManager: LocationManager? = null

    private lateinit var latitudeView: TextView
    private lateinit var longitudeView: TextView
    private var speed: Float = 50.0f

    //    private lateinit var speedView: TextView
    private lateinit var slider: Slider


    private var locationListener = LocationListener { location ->

        latitudeView.text = String.format("Latitude %.6f",location.latitude)
        longitudeView.text = String.format("Longitude %.6f",location.longitude)
        if (location.hasSpeed()){
            slider.addOnChangeListener { slider, value, fromUser ->
                     speed = value
            }

            if(location.speed > speed){
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    Toast.makeText(applicationContext, "You are too fast", Toast.LENGTH_SHORT).show()
                } else {
                    vibrator.vibrate(500)
                    Toast.makeText(applicationContext, "You are too fast", Toast.LENGTH_SHORT).show()
                }
            }
            }
        }



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_meter)
        /*
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(ImageView(this@SoundMeterActivity).apply {
                setImageResource(R.mipmap.ic_launcher)
            })
        })*/
        popupAlreadyDisplayed = savedInstanceState?.getBoolean(POPUP_DISPLAYED) ?: false
        latitudeView = findViewById(R.id.latitude)
        longitudeView = findViewById(R.id.longitude)
        slider = findViewById(R.id.speed2)
        speed = slider.value
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (supportFragmentManager.findFragmentByTag(TAG_PERMISSION_FRAGMENT) == null
                && !popupAlreadyDisplayed) {
                supportFragmentManager
                    .beginTransaction()
                    .add(PermissionPopupFragment(), TAG_PERMISSION_FRAGMENT)
                    .commit()
                popupAlreadyDisplayed = true
            }
        } else {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.requestLocationUpdates(
                LOCATION_UPDATE_MS,
                LOCATION_UPDATE_RADIUS,
                Criteria().apply {
                    accuracy = Criteria.ACCURACY_FINE
                }, locationListener, Looper.myLooper())


        }
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeUpdates(locationListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(POPUP_DISPLAYED, popupAlreadyDisplayed)
    }

}