package com.github.naz013.compassapp.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.naz013.compassapp.view.BaseCompassView

abstract class PageFragment : Fragment() {

    private var compassInterface: CompassInterface? = null
    private var compassView: BaseCompassView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (compassInterface == null) {
            compassInterface = context as CompassInterface?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        compassView = provideCompassView(context!!)
        return compassView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compassInterface?.addThemeListener {
            compassView?.palette = it
        }
        compassInterface?.addAngleListener {
            compassView?.degrees = it
        }
    }

    abstract fun provideCompassView(context: Context): BaseCompassView
}