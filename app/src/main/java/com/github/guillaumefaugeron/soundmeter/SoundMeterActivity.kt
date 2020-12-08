package com.github.guillaumefaugeron.soundmeter

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class SoundMeterActivity: AppCompatActivity() {

    companion object {
        const val POPUP_DISPLAYED = "popupAlreadyDisplayed"
        const val TAG_PERMISSION_FRAGMENT = "permissionDialogFragment"
        const val REQUEST_CODE_PERMISSION = 1
        const val LOCATION_UPDATE_MS = 10_000L
        const val LOCATION_UPDATE_RADIUS = 100f
    }

    private var popupAlreadyDisplayed = false

    private lateinit var startStopBtn: Button
    private lateinit var coordinatesView: TextView
    private lateinit var speedView: TextView

    val locationConsumer = Consumer<Location> { location ->
        coordinatesView.text = getString(R.string.location_template,
            String.format("%.6f", location.latitude),
            String.format("%.6f", location.longitude))

        speedView.text = getString(R.string.speed_template,
            String.format("%.2f", location.speed ?: 0))

        /*
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putInt(SoundMeterSettings.SETTING_SPEED, 60)
            .apply()
         */
    }

    var locationDisposable: Disposable? = null

    // region lifecycle

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
        startStopBtn = findViewById(R.id.start)
        startStopBtn.setOnClickListener {
            LocationService.startOrStop(this)
            syncUI() // TODO
        }
        popupAlreadyDisplayed = savedInstanceState?.getBoolean(POPUP_DISPLAYED) ?: false
        coordinatesView = findViewById(R.id.coordinates)
        speedView = findViewById(R.id.speed)
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

    // endregion

    // region menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_sound_meter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_settings -> {
                startActivity(Intent(this, LocationSettings::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // endregion

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
