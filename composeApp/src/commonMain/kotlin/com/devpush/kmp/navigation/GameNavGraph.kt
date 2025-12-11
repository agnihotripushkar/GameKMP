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
import com.devpush.features.bookmarklist.ui.collections.CollectionsScreen
import com.devpush.features.bookmarklist.ui.collections.CollectionDetailScreen
import com.devpush.features.userRatingsReviews.ui.statistics.StatisticsScreen
import kotlinx.serialization.Serializable

object GameNavGraph : BaseNavGraph {

    @Serializable
    sealed class Dest {

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
        navGraphBuilder.navigation<Dest.Root>(startDestination = Dest.Game) {
            composable<Dest.Game> {
                GameScreen(
                    modifier = modifier.fillMaxSize(),
                    onClick = {
                        navHostController.navigate(Dest.Details(it))
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