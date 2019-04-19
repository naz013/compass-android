package com.github.naz013.compassapp.utils

import com.github.naz013.compassapp.theming.ThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

fun utilModule() = module {
    single { Prefs(androidContext()) }
}

fun viewModels() = module {
    viewModel { ThemeViewModel() }
}

fun components(): List<Module> {
    return listOf(utilModule(), viewModels())
}