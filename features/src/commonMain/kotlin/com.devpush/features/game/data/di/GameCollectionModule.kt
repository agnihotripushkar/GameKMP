package com.devpush.features.game.data.di

import com.devpush.features.game.data.repository.GameCollectionRepositoryImpl
import com.devpush.features.game.domain.repository.GameCollectionRepository
import org.koin.dsl.module

fun getGameCollectionModule() = module {
    factory<GameCollectionRepository> { GameCollectionRepositoryImpl(get()) }
}