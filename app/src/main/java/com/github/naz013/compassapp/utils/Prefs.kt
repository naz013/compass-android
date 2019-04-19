package com.github.naz013.compassapp.utils

import android.content.Context

class Prefs(context: Context) {

    private val shared = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var appTheme: Int
        get() = shared.getInt(THEME, 0)
        set(value) = shared.edit().putInt(THEME, value).apply()

    companion object {
        private const val NAME = "compass_prefs"
        private const val THEME = "theme"
    }
}