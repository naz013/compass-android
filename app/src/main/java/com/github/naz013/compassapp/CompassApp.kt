package com.github.naz013.compassapp

import android.app.Application
import timber.log.Timber

class CompassApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}