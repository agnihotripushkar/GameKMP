package com.devpush.features.game.domain.di

import com.devpush.features.game.domain.usecase.SearchGamesUseCase
import com.devpush.features.game.domain.usecase.SearchGamesUseCaseImpl
import com.devpush.features.game.domain.usecase.FilterGamesUseCase
import com.devpush.features.game.domain.usecase.FilterGamesUseCaseImpl
import com.devpush.features.game.domain.usecase.GetAvailableFiltersUseCase
import com.devpush.features.game.domain.usecase.GetAvailableFiltersUseCaseImpl
import com.devpush.features.game.domain.usecase.FilterCollectionGamesUseCase
import org.koin.dsl.module

fun getGameDomainModule() = module {
    factory<SearchGamesUseCase> { SearchGamesUseCaseImpl() }
    factory<FilterGamesUseCase> { FilterGamesUseCaseImpl() }
    factory<GetAvailableFiltersUseCase> { GetAvailableFiltersUseCaseImpl() }
    factory { FilterCollectionGamesUseCase() }
}