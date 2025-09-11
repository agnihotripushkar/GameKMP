package com.devpush.features.game.ui.di

import com.devpush.features.game.ui.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getGameViewModelModule() = module {
    viewModel { GameViewModel(get()) }
}