package com.devpush.features.bookmarklist.di

import com.devpush.features.bookmarklist.ui.collections.CollectionDetailViewModel
import com.devpush.features.bookmarklist.ui.collections.CollectionsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun getGameCollectionViewModelModule() = module {
    viewModel { 
        CollectionsViewModel(
            getCollectionsUseCase = get(),
            createCollectionUseCase = get(),
            deleteCollectionUseCase = get(),
            updateCollectionUseCase = get(),
            initializeDefaultCollectionsUseCase = get()
        )
    }
    
    viewModel { (collectionId: String) ->
        CollectionDetailViewModel(
            collectionId = collectionId,
            getCollectionsUseCase = get(),
            addGameToCollectionUseCase = get(),
            removeGameFromCollectionUseCase = get(),
            updateCollectionUseCase = get(),
            getGamesWithUserDataUseCase = get(),
            setUserRatingUseCase = get(),
            filterCollectionGamesUseCase = get()
        )
    }
}