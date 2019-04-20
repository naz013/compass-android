package com.github.naz013.compassapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.naz013.compassapp.theming.Palette
import com.github.naz013.compassapp.theming.ThemeViewModel
import com.github.naz013.compassapp.utils.Compass
import com.github.naz013.compassapp.view.BaseCompassView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: ThemeViewModel by viewModel()
    private var compassView: BaseCompassView? = null
    private var compass: Compass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compass = Compass.newInstance(this)
        compass?.angle?.observe(this, Observer {
            if (it != null) {
                compassView?.degrees = it
            }
        })

        setContentView(R.layout.activity_main)

        compassView = findViewById(R.id.dottedView)

        viewModel.palette.observe(this, Observer {
            if (it != null) {
                applyTheme(it)
            }
        })
    }

    private fun applyTheme(palette: Palette) {
        compassView?.palette = palette
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
