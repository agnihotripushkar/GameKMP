package com.devpush.features.gameDetails.ui.di

import com.devpush.features.gameDetails.ui.GameDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getGameDetailsViewModelModule() = module {
    viewModel { 
        GameDetailsViewModel(
            gameDetailsRepository = get(),
            getCollectionsUseCase = get(),
            addGameToCollectionUseCase = get(),
            getGameWithUserDataUseCase = get(),
            setUserRatingUseCase = get(),
            setUserReviewUseCase = get(),
            deleteUserRatingUseCase = get(),
            deleteUserReviewUseCase = get(),
            shareManager = get()
        ) 
    }
}