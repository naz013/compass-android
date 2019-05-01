package com.github.naz013.compassapp.pages

import android.content.Context
import com.github.naz013.compassapp.view.BaseCompassView
import com.github.naz013.compassapp.view.DottedCompassView

class DottedFragment : PageFragment() {

    override fun provideCompassView(context: Context): BaseCompassView {
        return DottedCompassView(context)
    }

    companion object {
        fun newInstance(): DottedFragment = DottedFragment()
    }
}