package com.devpush.kmp.di

import com.devpush.coreDatabase.di.getCoreDatabaseModule
import com.devpush.coreNetwork.di.getCoreNetworkModule
import com.devpush.features.game.data.di.getGameModule
import com.devpush.features.game.domain.di.getGameDomainModule
import com.devpush.features.game.ui.di.getGameViewModelModule
import com.devpush.features.gameDetails.data.di.getGameDetailsModule
import com.devpush.features.gameDetails.ui.di.getGameDetailsViewModelModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun intiKoin(koinApplication:((KoinApplication) -> Unit)? = null){
    startKoin {
        koinApplication?.invoke(this)
        modules(
            getCoreNetworkModule(),
            getCoreDatabaseModule(),

            getGameDomainModule(),
            getGameModule(),
            getGameViewModelModule(),

            getGameDetailsModule(),
            getGameDetailsViewModelModule(),

        )

    }
}