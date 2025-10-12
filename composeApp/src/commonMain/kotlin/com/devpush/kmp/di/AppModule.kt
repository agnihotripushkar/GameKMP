package com.devpush.kmp.di

import com.devpush.kmp.service.AppInitializationService
import com.devpush.kmp.service.AppInitializationServiceImpl
import org.koin.dsl.module

fun getAppModule() = module {
    single<AppInitializationService> { AppInitializationServiceImpl(get()) }
}