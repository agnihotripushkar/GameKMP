package com.devpush.features.collections.di

import com.devpush.features.collections.ui.collections.CollectionDetailViewModel
import com.devpush.features.collections.ui.collections.CollectionsViewModel
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