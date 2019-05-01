package com.github.naz013.compassapp.pages

import android.content.Context
import com.github.naz013.compassapp.view.BaseCompassView
import com.github.naz013.compassapp.view.DottedCompassView
import com.github.naz013.compassapp.view.SimpleOneCompassView

class Simple1Fragment : PageFragment() {

    override fun provideCompassView(context: Context): BaseCompassView {
        return SimpleOneCompassView(context)
    }

    companion object {
        fun newInstance(): Simple1Fragment = Simple1Fragment()
    }
}