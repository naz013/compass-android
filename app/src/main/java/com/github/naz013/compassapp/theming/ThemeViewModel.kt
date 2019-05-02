package com.github.naz013.compassapp.theming

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.compassapp.utils.Prefs
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ThemeViewModel : ViewModel(), KoinComponent {

    private val prefs: Prefs by inject()

    private val _palette = MutableLiveData<Palette>()
    val palette: LiveData<Palette> = _palette

    init {
        _palette.postValue(paletteFromTheme(prefs.appTheme))
    }

    fun setTheme(theme: Int) {
        prefs.appTheme = theme
        _palette.postValue(paletteFromTheme(theme))
    }

    private fun paletteFromTheme(theme: Int): Palette {
        return COLORS[theme]
    }

    fun setLastPage(page: Int) {
        prefs.lastPage = page
    }

    fun lastPage(): Int {
        return prefs.lastPage
    }

    companion object {
        private val COLORS = arrayOf(
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#F63205"), parse("#AD2005")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#F63205"), parse("#AD2005"))
        )

        @ColorInt
        private fun parse(@Size(min = 1) colorHex: String): Int = Color.parseColor(colorHex)
    }
}