package com.devpush.features.bookmarklist.di

import com.devpush.features.game.ui.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getGameViewModelModule() = module {
    viewModel { 
        GameViewModel(
            gameRepository = get(),
            searchGamesUseCase = get(),
            filterGamesUseCase = get(),
            getAvailableFiltersUseCase = get(),
            getCollectionsUseCase = get(),
            addGameToCollectionUseCase = get()
        ) 
    }
}