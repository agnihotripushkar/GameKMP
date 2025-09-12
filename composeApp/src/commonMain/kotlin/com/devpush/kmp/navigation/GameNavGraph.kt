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
                    })
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
        }
    }
}