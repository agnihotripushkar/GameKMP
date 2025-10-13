package com.devpush.features.userRatingsReviews.di

import com.devpush.features.userRatingsReviews.data.repository.UserRatingReviewRepositoryImpl
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository
import com.devpush.features.userRatingsReviews.domain.usecase.DeleteUserRatingUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.DeleteUserReviewUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetGamesWithUserDataUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetGameWithUserDataUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetRecentUserActivityUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetUserRatingStatsUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetUserRatingUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.GetUserReviewUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.SetUserRatingUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.SetUserReviewUseCase
import com.devpush.features.userRatingsReviews.domain.validation.InputSanitizer
import com.devpush.features.userRatingsReviews.domain.validation.UserRatingReviewValidator
import com.devpush.features.userRatingsReviews.ui.statistics.StatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getUserRatingReviewModule() = module {
    // Repository
    single<UserRatingReviewRepository> { UserRatingReviewRepositoryImpl(get()) }
    
    // Validation
    single { UserRatingReviewValidator() }
    single { InputSanitizer() }
    
    // Use Cases
    single { GetGameWithUserDataUseCase(get()) }
    single { GetGamesWithUserDataUseCase(get()) }
    single { GetUserRatingUseCase(get()) }
    single { GetUserReviewUseCase(get()) }
    single { GetUserRatingStatsUseCase(get()) }
    single { GetRecentUserActivityUseCase(get()) }
    single { SetUserRatingUseCase(get(), get()) }
    single { SetUserReviewUseCase(get(), get(), get()) }
    single { DeleteUserRatingUseCase(get()) }
    single { DeleteUserReviewUseCase(get()) }
    
    // ViewModels
    viewModel { StatisticsViewModel(get(), get()) }
}