package com.github.naz013.compassapp.utils

import android.content.Context

class Prefs(context: Context) {

    private val shared = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    companion object {
        private const val NAME = "compass_prefs"

        private var INSTANCE: Prefs? = null

        fun getInstance(context: Context): Prefs {
            val inst: Prefs = INSTANCE ?: Prefs(context)
            INSTANCE = inst
            return inst
        }
    }
}