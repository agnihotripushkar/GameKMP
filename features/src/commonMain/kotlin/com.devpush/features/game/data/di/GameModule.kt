package com.devpush.features.game.data.di

import com.devpush.features.game.data.repository.GameRepositoryImpl
import com.devpush.features.game.domain.repository.GameRepository
import org.koin.dsl.module

fun getGameModule() = module {
    factory <GameRepository>{ GameRepositoryImpl(get(), get()) }
}