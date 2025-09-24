package com.devpush.coreNetwork.di

import com.devpush.coreNetwork.api.Secrets
import com.devpush.coreNetwork.apiService.ApiService
import com.devpush.coreNetwork.client.KtorClient
import org.koin.dsl.module

fun getCoreNetworkModule() = module {
    single { ApiService(httpClient = KtorClient.getInstance(), apiKey = Secrets.apiKey) }
}