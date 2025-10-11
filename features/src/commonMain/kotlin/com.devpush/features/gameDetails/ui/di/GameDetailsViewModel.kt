package com.devpush.features.gameDetails.ui.di

import com.devpush.features.gameDetails.ui.GameDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getGameDetailsViewModelModule() = module {
    viewModel { GameDetailsViewModel(get(), get(), get()) }
}