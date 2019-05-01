package com.github.naz013.compassapp.pages

import com.github.naz013.compassapp.theming.Palette

interface CompassInterface {
    fun addAngleListener(listener: (degree: Float) -> Unit)

    fun addThemeListener(listener: (palette: Palette) -> Unit)
}