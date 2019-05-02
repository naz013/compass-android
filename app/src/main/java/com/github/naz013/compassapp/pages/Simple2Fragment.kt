package com.github.naz013.compassapp.pages

import android.content.Context
import com.github.naz013.compassapp.view.BaseCompassView
import com.github.naz013.compassapp.view.SimpleTwoCompassView

class Simple2Fragment : PageFragment() {

    override fun provideCompassView(context: Context): BaseCompassView {
        return SimpleTwoCompassView(context)
    }

    companion object {
        fun newInstance(): Simple2Fragment = Simple2Fragment()
    }
}