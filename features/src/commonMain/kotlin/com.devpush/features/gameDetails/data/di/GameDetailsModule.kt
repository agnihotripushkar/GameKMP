package com.devpush.features.gameDetails.data.di

import com.devpush.features.gameDetails.data.repository.GameDetailsRepositoryImpl
import com.devpush.features.gameDetails.data.sharing.DefaultShareManager
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository
import com.devpush.features.gameDetails.domain.sharing.ShareManager
import org.koin.dsl.module

fun getGameDetailsModule() = module {
    factory <GameDetailsRepository>{ GameDetailsRepositoryImpl(get(),get()) }
    factory <ShareManager>{ DefaultShareManager() }
}