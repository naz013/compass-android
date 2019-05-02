package com.github.naz013.compassapp

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.github.naz013.compassapp.pages.*
import com.github.naz013.compassapp.theming.Palette
import com.github.naz013.compassapp.theming.ThemeViewModel
import com.github.naz013.compassapp.utils.Compass
import github.chenupt.springindicator.SpringIndicator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), CompassInterface {

    private val viewModel: ThemeViewModel by viewModel()
    private val mListeners = mutableListOf<(Float) -> Unit>()
    private val mThemeListeners = mutableListOf<(Palette) -> Unit>()
    private var bgView: View? = null
    private var indicator: SpringIndicator? = null
    private var compass: Compass? = null
    private var palette: Palette? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compass = Compass.newInstance(this)
        compass?.angle?.observe(this, Observer { angle ->
            if (angle != null) {
                mListeners.forEach { it.invoke(angle) }
            }
        })

        setContentView(R.layout.activity_main)
        bgView = findViewById(R.id.bgView)

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPager.offscreenPageLimit = 4
        viewPager.adapter = Adapter()
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                viewModel.setLastPage(position)
            }
        })
        viewPager.setCurrentItem(viewModel.lastPage(), true)

        indicator = findViewById(R.id.indicator)
        indicator?.setViewPager(viewPager)

        viewModel.palette.observe(this, Observer {
            if (it != null) {
                applyTheme(it)
            }
        })
    }

    private fun applyTheme(palette: Palette) {
        this.palette = palette

        mThemeListeners.forEach { it.invoke(palette) }
        bgView?.setBackgroundColor(palette.colorPrimary)
        window.statusBarColor = palette.colorPrimary
        if (Build.VERSION.SDK_INT >= 23) {
            if (palette.isDark) {
                bgView?.systemUiVisibility = -View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                bgView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onResume() {
        super.onResume()
        compass?.start()
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mListeners.clear()
        mThemeListeners.clear()
    }

    override fun addAngleListener(listener: (degree: Float) -> Unit) {
        mListeners.add(listener)
    }

    override fun addThemeListener(listener: (palette: Palette) -> Unit) {
        mThemeListeners.add(listener)
        palette?.let { listener.invoke(it) }
    }

    inner class Adapter : FragmentStatePagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    DottedFragment.newInstance()
                }
                1 -> {
                    Simple1Fragment.newInstance()
                }
                2 -> {
                    LabeledFragment.newInstance()
                }
                else -> {
                    Simple2Fragment.newInstance()
                }
            }
        }

        override fun getCount(): Int {
            return 4
        }
    }
}
