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

    var hintShowed: Boolean
        get() = shared.getBoolean(HINT_SHOWED, false)
        set(value) = shared.edit().putBoolean(HINT_SHOWED, value).apply()

    companion object {
        private const val NAME = "compass_prefs"
        private const val THEME = "theme"
        private const val LAST_PAGE = "last_page"
        private const val HINT_SHOWED = "hint_showed"
    }
}