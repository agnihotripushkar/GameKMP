package com.devpush.kmp.di

import com.devpush.coreDatabase.di.getCoreDatabaseModule
import com.devpush.coreNetwork.di.getCoreNetworkModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun intiKoin(koinApplication:((KoinApplication) -> Unit)? = null){
    startKoin {
        koinApplication?.invoke(this)
        modules(
            getCoreNetworkModule(),
            getCoreDatabaseModule()
        )

    }
}