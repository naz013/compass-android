package com.github.naz013.compassapp.theming

import androidx.annotation.ColorInt

data class Palette(
    @ColorInt
    val colorPrimary: Int,
    @ColorInt
    val colorOnPrimary: Int,
    @ColorInt
    val colorSecondary: Int,
    @ColorInt
    val colorSecondarySolid: Int,
    val isDark: Boolean = true
)