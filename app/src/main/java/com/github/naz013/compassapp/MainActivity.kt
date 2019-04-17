package com.github.naz013.compassapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import timber.log.Timber

class MainActivity : AppCompatActivity(), Compass.CompassListener {

    private var compass: Compass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compass = Compass.newInstance(this, this)
        compass?.start()

        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        super.onDestroy()
        compass?.stop()
    }

    override fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float) {
        Timber.d("onOrientationChanged: az -> $azimuth, pitch -> $pitch, roll -> $roll")
    }
}
