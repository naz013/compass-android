package com.github.naz013.compassapp

import android.app.Application
import com.github.naz013.compassapp.utils.components
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class CompassApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin(this, components())
    }
}