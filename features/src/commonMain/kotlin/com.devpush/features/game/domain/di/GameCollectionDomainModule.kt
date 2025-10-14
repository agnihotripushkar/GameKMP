package com.devpush.features.game.domain.di

import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCaseImpl
import com.devpush.features.game.domain.usecase.CreateCollectionUseCase
import com.devpush.features.game.domain.usecase.CreateCollectionUseCaseImpl
import com.devpush.features.game.domain.usecase.DeleteCollectionUseCase
import com.devpush.features.game.domain.usecase.DeleteCollectionUseCaseImpl
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.GetCollectionsUseCaseImpl
import com.devpush.features.game.domain.usecase.InitializeDefaultCollectionsUseCase
import com.devpush.features.game.domain.usecase.InitializeDefaultCollectionsUseCaseImpl
import com.devpush.features.game.domain.usecase.RemoveGameFromCollectionUseCase
import com.devpush.features.game.domain.usecase.RemoveGameFromCollectionUseCaseImpl
import com.devpush.features.game.domain.usecase.ManageCollectionStatusUseCase
import com.devpush.features.game.domain.usecase.ManageCollectionStatusUseCaseImpl
import com.devpush.features.game.domain.usecase.UpdateCollectionUseCase
import com.devpush.features.game.domain.usecase.UpdateCollectionUseCaseImpl
import com.devpush.features.game.domain.service.CollectionInitializationService
import com.devpush.features.game.domain.service.CollectionInitializationServiceImpl
import org.koin.dsl.module

fun getGameCollectionDomainModule() = module {
    factory<GetCollectionsUseCase> { GetCollectionsUseCaseImpl(get()) }
    factory<CreateCollectionUseCase> { CreateCollectionUseCaseImpl(get()) }
    factory<DeleteCollectionUseCase> { DeleteCollectionUseCaseImpl(get()) }
    factory<AddGameToCollectionUseCase> { AddGameToCollectionUseCaseImpl(get()) }
    factory<RemoveGameFromCollectionUseCase> { RemoveGameFromCollectionUseCaseImpl(get()) }
    factory<UpdateCollectionUseCase> { UpdateCollectionUseCaseImpl(get()) }
    factory<InitializeDefaultCollectionsUseCase> { InitializeDefaultCollectionsUseCaseImpl(get()) }
    factory<ManageCollectionStatusUseCase> { ManageCollectionStatusUseCaseImpl(get()) }
    single<CollectionInitializationService> { CollectionInitializationServiceImpl(get()) }
}