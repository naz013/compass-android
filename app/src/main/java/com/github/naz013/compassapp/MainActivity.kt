package com.github.naz013.compassapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.github.naz013.compassapp.pages.*
import com.github.naz013.compassapp.theming.Palette
import com.github.naz013.compassapp.theming.ThemeViewModel
import com.github.naz013.compassapp.utils.Compass
import com.google.android.material.bottomsheet.BottomSheetDialog
import github.chenupt.springindicator.SpringIndicator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), CompassInterface {

    private val viewModel: ThemeViewModel by viewModel()
    private val mListeners = mutableListOf<(Float) -> Unit>()
    private val mThemeListeners = mutableListOf<(Palette) -> Unit>()
    private var bgView: View? = null
    private var indicator: SpringIndicator? = null
    private var themeButton: AppCompatImageView? = null
    private var compass: Compass? = null
    private var palette: Palette? = null
    private var mDialog: BottomSheetDialog? = null

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
        themeButton = findViewById(R.id.themeButton)
        themeButton?.setOnClickListener { showThemeDialog() }

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
        viewModel.showHint.observe(this, Observer {
            if (it != null && it) Toast.makeText(this, getString(R.string.hint_message), Toast.LENGTH_LONG).show()
        })
    }

    private fun showThemeDialog() {
        val themes = viewModel.getThemes()

        val dialog = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.dialog_theme_list, null)

        palette?.let {
            sheetView.findViewById<View>(R.id.dialogBgView).setBackgroundColor(it.colorPrimary)
        }
        val themeList = sheetView.findViewById<RecyclerView>(R.id.themeList)
        themeList.layoutManager = GridLayoutManager(this, 4)
        themeList.adapter = ThemeAdapter(themes)

        dialog.setContentView(sheetView)
        dialog.show()
        mDialog = dialog
    }

    private fun applyTheme(palette: Palette) {
        this.palette = palette

        mThemeListeners.forEach { it.invoke(palette) }

        themeButton?.setColorFilter(palette.colorOnPrimary, android.graphics.PorterDuff.Mode.SRC_IN)
        bgView?.setBackgroundColor(palette.colorPrimary)
        window.statusBarColor = palette.colorPrimary
        indicator?.setIndicatorColor(palette.colorSecondary)

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

    private fun selectTheme(position: Int) {
        viewModel.setTheme(position)
        mDialog?.dismiss()
    }

    inner class ThemeAdapter(val list: List<Palette>) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(parent)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(list[position])
        }
    }

    inner class Holder(parent: ViewGroup) : RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.list_item_theme, parent, false)) {
        private var borderView: View
        private var bgPrimary: View
        private var bgSecondary: View

        init {
            itemView.setOnClickListener { selectTheme(adapterPosition) }
            borderView = itemView.findViewById(R.id.borderView)
            bgPrimary = itemView.findViewById(R.id.bgPrimary)
            bgSecondary = itemView.findViewById(R.id.bgSecondary)
        }

        fun bind(palette: Palette) {
            borderView.setBackgroundColor(palette.colorOnPrimary)
            bgPrimary.setBackgroundColor(palette.colorPrimary)
            bgSecondary.setBackgroundColor(palette.colorSecondary)
        }
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
