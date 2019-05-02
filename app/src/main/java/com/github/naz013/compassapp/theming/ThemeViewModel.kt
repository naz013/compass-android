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

    private val _showHint = MutableLiveData<Boolean>()
    val showHint: LiveData<Boolean> = _showHint

    init {
        _palette.postValue(paletteFromTheme(prefs.appTheme))
        if (!prefs.hintShowed) {
            _showHint.postValue(true)
            prefs.hintShowed = true
        }
    }

    fun getThemes(): List<Palette> {
        return COLORS.toList()
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
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#9c27b0"), parse("#6a0080")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#2196f3"), parse("#0069c0")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#009688"), parse("#00675b")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#8bc34a"), parse("#5a9216")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#ffc107"), parse("#c79100")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#ff5722"), parse("#c41c00")),
            Palette(parse("#000000"), parse("#FFFFFF"), parse("#e91e63"), parse("#b0003a")),

            Palette(parse("#FFFFFF"), parse("#000000"), parse("#F63205"), parse("#AD2005")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#9c27b0"), parse("#6a0080")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#2196f3"), parse("#0069c0")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#009688"), parse("#00675b")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#8bc34a"), parse("#5a9216")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#ffc107"), parse("#c79100")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#ff5722"), parse("#c41c00")),
            Palette(parse("#FFFFFF"), parse("#000000"), parse("#e91e63"), parse("#b0003a"))
        )

        @ColorInt
        private fun parse(@Size(min = 1) colorHex: String): Int = Color.parseColor(colorHex)
    }
}