package com.devpush.kmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.devpush.features.game.ui.GameScreen
import com.devpush.features.gameDetails.ui.GameDetailsScreen
import com.devpush.features.game.ui.collections.CollectionsScreen
import com.devpush.features.game.ui.collections.CollectionDetailScreen
import com.devpush.features.userRatingsReviews.ui.statistics.StatisticsScreen
import kotlinx.serialization.Serializable

import com.russhwolf.settings.Settings

object GameNavGraph : BaseNavGraph {

    @Serializable
    sealed class Dest {

        @Serializable
        data object Splash : Dest()

        @Serializable
        data object Onboarding : Dest()

        @Serializable
        data object Root : Dest()

        @Serializable
        data object Game : Dest()

        @Serializable
        data class Details(val id: Int) : Dest()

        @Serializable
        data object Collections : Dest()

        @Serializable
        data class CollectionDetail(val collectionId: String) : Dest()

        @Serializable
        data object Statistics : Dest()
    }

    override fun build(
        modifier: Modifier,
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder
    ) {
        val settings = Settings()

        navGraphBuilder.navigation<Dest.Root>(startDestination = Dest.Splash) {
            composable<Dest.Splash> {
                com.devpush.kmp.ui.onboarding.SplashScreen {
                    val hasSeenOnboarding = settings.getBoolean("has_seen_onboarding", false)
                    if (hasSeenOnboarding) {
                        navHostController.navigate(Dest.Game) {
                            popUpTo(Dest.Splash) { inclusive = true }
                        }
                    } else {
                        navHostController.navigate(Dest.Onboarding) {
                            popUpTo(Dest.Splash) { inclusive = true }
                        }
                    }
                }
            }

            composable<Dest.Onboarding> {
                com.devpush.kmp.ui.onboarding.OnboardingScreen {
                    settings.putBoolean("has_seen_onboarding", true)
                    navHostController.navigate(Dest.Game) {
                        popUpTo(Dest.Onboarding) { inclusive = true }
                    }
                }
            }

            composable<Dest.Game> {
                GameScreen(
                    modifier = modifier.fillMaxSize(),
                    onClick = {
                        navHostController.navigate(Dest.Details(it))
                    },
                    onNavigateToCollections = {
                        navHostController.navigate(Dest.Collections)
                    },
                    onNavigateToStatistics = {
                        navHostController.navigate(Dest.Statistics)
                    }
                )
            }

            composable<Dest.Details> {
                val args = it.toRoute<Dest.Details>()
                GameDetailsScreen(
                    modifier = modifier.fillMaxSize(),
                    id = args.id.toString(),
                    onBackClick = {
                        navHostController.popBackStack()
                    })
            }

            composable<Dest.Collections> {
                CollectionsScreen(
                    modifier = modifier.fillMaxSize(),
                    onCollectionClick = { collectionId ->
                        navHostController.navigate(Dest.CollectionDetail(collectionId))
                    },
                    onNavigateBack = {
                        navHostController.popBackStack()
                    }
                )
            }

            composable<Dest.CollectionDetail> {
                val args = it.toRoute<Dest.CollectionDetail>()
                CollectionDetailScreen(
                    collectionId = args.collectionId,
                    modifier = modifier.fillMaxSize(),
                    onNavigateBack = {
                        navHostController.popBackStack()
                    },
                    onGameClick = { gameId ->
                        navHostController.navigate(Dest.Details(gameId))
                    }
                )
            }

            composable<Dest.Statistics> {
                StatisticsScreen(
                    modifier = modifier.fillMaxSize(),
                    onNavigateBack = {
                        navHostController.popBackStack()
                    },
                    onGameClick = { gameId ->
                        navHostController.navigate(Dest.Details(gameId))
                    }
                )
            }
        }
    }
}