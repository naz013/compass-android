package com.github.naz013.compassapp.utils

import android.content.Context

class Prefs(context: Context) {

    private val shared = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var appTheme: Int
        get() = shared.getInt(THEME, 0)
        set(value) = shared.edit().putInt(THEME, value).apply()

    var lastPage: Int
        get() = shared.getInt(LAST_PAGE, 0)
        set(value) = shared.edit().putInt(LAST_PAGE, value).apply()

    companion object {
        private const val NAME = "compass_prefs"
        private const val THEME = "theme"
        private const val LAST_PAGE = "last_page"
    }
}