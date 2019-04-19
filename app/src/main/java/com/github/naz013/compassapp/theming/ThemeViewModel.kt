package com.github.naz013.compassapp.theming

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.compassapp.utils.Prefs
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ThemeViewModel : ViewModel(), KoinComponent {

    private val prefs: Prefs by inject()

    private val _palette = MutableLiveData<Palette>()
    private val palette: LiveData<Palette> = _palette

    init {
        _palette.postValue(paletteFromTheme(prefs.appTheme))
    }

    fun setTheme(theme: Int) {
        prefs.appTheme = theme
    }

    private fun paletteFromTheme(theme: Int): Palette {
    }

    companion object {

    }
}