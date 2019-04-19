package com.github.naz013.compassapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.naz013.compassapp.theming.ThemeViewModel
import com.github.naz013.compassapp.utils.Compass
import com.github.naz013.compassapp.view.DottedCompassView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: ThemeViewModel by viewModel()
    private lateinit var dottedCompassView: DottedCompassView
    private var compass: Compass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compass = Compass.newInstance(this)
        compass?.angle?.observe(this, Observer {
            if (it != null) {
                dottedCompassView.degrees = it
            }
        })

        setContentView(R.layout.activity_main)

        dottedCompassView = findViewById(R.id.dottedView)
    }

    override fun onResume() {
        super.onResume()
        compass?.start()
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }
}
